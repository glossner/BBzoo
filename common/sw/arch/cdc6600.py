import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import FixedLengthFormat
from architecture.operations import FixedPointOperations

class Cdc6600Assembler(BaseAssembler, GeneralRegisterArchitecture, FixedLengthFormat, FixedPointOperations):
    word_width = 60
    address_width = 18
    num_gprs = 24
    num_fprs = 0
    op_code_width = 6
    instruction_width = 60
    has_multiply_divide = False

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

        # Replace double spaces
        line = re.sub(r'\s+', ' ', line)

        # 1. HLT
        if line.upper() == "HLT":
            return [0]

        # 2. Ai = Bj + K
        m = re.match(r'^A([0-7])\s*=\s*B([0-7])\s*\+\s*(\S+)', line, re.IGNORECASE)
        if m:
            i = int(m.group(1))
            j = int(m.group(2))
            K = self.parse_numeric_or_symbol(m.group(3))
            # Encode in 60-bit
            val = (1 << 54) | (i << 51) | (j << 48) | ((K & 0x3FFFF) << 27)
            return [val]

        # 3. Xi = Xj + Xk
        m = re.match(r'^X([0-7])\s*=\s*X([0-7])\s*\+\s*X([0-7])', line, re.IGNORECASE)
        if m:
            i = int(m.group(1))
            j = int(m.group(2))
            k = int(m.group(3))
            val = (2 << 54) | (i << 51) | (j << 48) | (k << 45)
            return [val]

        # 4. Xi = Xj - Xk
        m = re.match(r'^X([0-7])\s*=\s*X([0-7])\s*-\s*X([0-7])', line, re.IGNORECASE)
        if m:
            i = int(m.group(1))
            j = int(m.group(2))
            k = int(m.group(3))
            val = (3 << 54) | (i << 51) | (j << 48) | (k << 45)
            return [val]

        # Check if raw value
        try:
            return [self.parse_numeric_or_symbol(line)]
        except ValueError:
            raise Exception(f"Unknown CDC 6600 instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled CDC 6600 Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFFFFFFFFF:015X}")
        return "\n".join(out) + "\n"
