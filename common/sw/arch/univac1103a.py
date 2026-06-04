import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import AccumulatorArchitecture
from architecture.instruction_format import FixedLengthFormat
from architecture.operations import FixedPointOperations

class Univac1103AAssembler(BaseAssembler, AccumulatorArchitecture, FixedLengthFormat, FixedPointOperations):
    word_width = 36
    address_width = 15
    has_link_bit = False
    op_code_width = 6
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
            return [14 << 30]

        if mnemonic in ["TP", "ADD", "SUB"]:
            op = 11 if mnemonic == "TP" else 12 if mnemonic == "ADD" else 13
            u_str = parts[1].replace(',', '').strip()
            v_str = parts[2].strip()
            u = self.parse_numeric_or_symbol(u_str)
            v = self.parse_numeric_or_symbol(v_str)
            return [(op << 30) | ((u & 0x7FFF) << 15) | (v & 0x7FFF)]

        try:
            return [self.parse_numeric_or_symbol(line)]
        except ValueError:
            raise Exception(f"Unknown Univac 1103A instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled Univac 1103A Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFFF:09X}")
        return "\n".join(out) + "\n"
