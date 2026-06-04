import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import AccumulatorArchitecture
from architecture.instruction_format import FixedLengthFormat
from architecture.operations import FixedPointOperations

class Ibm1401Assembler(BaseAssembler, AccumulatorArchitecture, FixedLengthFormat, FixedPointOperations):
    word_width = 36
    address_width = 12
    has_link_bit = False
    op_code_width = 8
    instruction_width = 36
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

        parts = line.split()
        mnemonic = parts[0].upper()

        if mnemonic == "HLT":
            return [4 << 24]

        if mnemonic in ["MC", "A", "S"]:
            op = 1 if mnemonic == "MC" else 2 if mnemonic == "A" else 3
            op1_str = parts[1].replace(',', '').strip()
            op2_str = parts[2].strip()
            a_addr = self.parse_numeric_or_symbol(op1_str)
            b_addr = self.parse_numeric_or_symbol(op2_str)
            return [(op << 24) | ((a_addr & 0xFFF) << 12) | (b_addr & 0xFFF)]

        try:
            return [self.parse_numeric_or_symbol(line)]
        except ValueError:
            raise Exception(f"Unknown IBM 1401 instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled IBM 1401 Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFFF:09X}")
        return "\n".join(out) + "\n"
