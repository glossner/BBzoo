import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import VectorArchitecture
from architecture.instruction_format import FixedLengthFormat
from architecture.operations import FixedPointOperations, FloatingPointOperations, VectorOperations

class Cray1Assembler(BaseAssembler, VectorArchitecture, FixedLengthFormat, FixedPointOperations, FloatingPointOperations, VectorOperations):
    word_width = 64
    address_width = 24
    num_gprs = 8
    num_fprs = 8
    vector_length = 64
    num_vec_regs = 8
    op_code_width = 7
    instruction_width = 16
    has_multiply_divide = True
    fpu_precision = 64
    has_chaining = True

    def get_arch_instruction_size(self, line):
        return 1

    def parse_numeric_or_symbol(self, val):
        val = val.strip()
        if val in self.symbols:
            return self.symbols[val]
        return self.parse_numeric(val)

    def assemble_instruction(self, line):
        line = line.strip()
        if not line:
            return []

        if line.upper() == "HLT":
            return [0x0200]

        # VL = A<j>
        vl_match = re.match(r'^VL\s*=\s*A(\d+)', line, re.IGNORECASE)
        if vl_match:
            j = int(vl_match.group(1))
            return [(0 << 12) | (0 << 9) | (0 << 6) | (j << 3) | 0]

        # A<i> = <imm>
        li_match = re.match(r'^A(\d+)\s*=\s*(.+)', line, re.IGNORECASE)
        if li_match:
            h = int(li_match.group(1))
            imm = self.parse_numeric_or_symbol(li_match.group(2))
            return [(4 << 12) | (h << 9) | (imm & 0x1FF)]

        # V<i> = mem[A<j>]
        vload_match = re.match(r'^V(\d+)\s*=\s*mem\s*\[\s*A(\d+)\s*\]', line, re.IGNORECASE)
        if vload_match:
            i = int(vload_match.group(1))
            j = int(vload_match.group(2))
            return [(2 << 12) | (0 << 9) | (i << 6) | (j << 3) | 0]

        # mem[A<j>] = V<i>
        vstore_match = re.match(r'^mem\s*\[\s*A(\d+)\s*\]\s*=\s*V(\d+)', line, re.IGNORECASE)
        if vstore_match:
            j = int(vstore_match.group(1))
            i = int(vstore_match.group(2))
            return [(2 << 12) | (1 << 9) | (i << 6) | (j << 3) | 0]

        # V<i> = V<j> + V<k>
        vadd_match = re.match(r'^V(\d+)\s*=\s*V(\d+)\s*\+\s*V(\d+)', line, re.IGNORECASE)
        if vadd_match:
            i = int(vadd_match.group(1))
            j = int(vadd_match.group(2))
            k = int(vadd_match.group(3))
            return [(3 << 12) | (2 << 9) | (i << 6) | (j << 3) | k]

        # Check if raw value
        try:
            return [self.parse_numeric_or_symbol(line)]
        except ValueError:
            raise Exception(f"Unknown Cray-1 instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled Cray-1 Hex File")
        for val in values:
            # 64-bit format: 16 hex characters
            out.append(f"{val & 0xFFFFFFFFFFFFFFFF:016X}")
        return "\n".join(out) + "\n"
