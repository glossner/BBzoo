import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import AccumulatorArchitecture
from architecture.instruction_format import FixedLengthFormat
from architecture.operations import FixedPointOperations

class PrincetoniasAssembler(BaseAssembler, AccumulatorArchitecture, FixedLengthFormat, FixedPointOperations):
    word_width = 40
    address_width = 12
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
            return [5 << 12]

        if mnemonic in ["LD", "ST", "ADD", "SUB"]:
            op = 1 if mnemonic == "LD" else 2 if mnemonic == "ST" else 3 if mnemonic == "ADD" else 4
            addr = self.parse_numeric_or_symbol(parts[1])
            return [(op << 12) | (addr & 0xFFF)]

        try:
            return [self.parse_numeric_or_symbol(line)]
        except ValueError:
            raise Exception(f"Unknown Princetonias instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled Princetonias Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFFFF:010X}")
        return "\n".join(out) + "\n"
