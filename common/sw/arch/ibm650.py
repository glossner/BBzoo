import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import AccumulatorArchitecture
from architecture.instruction_format import FixedLengthFormat
from architecture.operations import FixedPointOperations

class Ibm650Assembler(BaseAssembler, AccumulatorArchitecture, FixedLengthFormat, FixedPointOperations):
    word_width = 40
    address_width = 16
    has_link_bit = False
    op_code_width = 8
    instruction_width = 40
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
            return [(4 << 32)]

        if mnemonic in ["LD", "ADD", "ST"]:
            op = 1 if mnemonic == "LD" else 2 if mnemonic == "ADD" else 3
            # Parse data_addr and next_addr (separated by space/comma)
            op1_str = parts[1].replace(',', '').strip()
            op2_str = parts[2].strip()
            data_addr = self.parse_numeric_or_symbol(op1_str)
            next_addr = self.parse_numeric_or_symbol(op2_str)
            return [(op << 32) | ((data_addr & 0xFFFF) << 16) | (next_addr & 0xFFFF)]

        try:
            return [self.parse_numeric_or_symbol(line)]
        except ValueError:
            raise Exception(f"Unknown IBM 650 instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled IBM 650 Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFFFF:010X}")
        return "\n".join(out) + "\n"
