import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import AccumulatorArchitecture
from architecture.instruction_format import FixedLengthFormat
from architecture.operations import FixedPointOperations

class CambridgeedsacAssembler(BaseAssembler, AccumulatorArchitecture, FixedLengthFormat, FixedPointOperations):
    word_width = 17
    address_width = 10
    has_link_bit = False
    op_code_width = 5
    instruction_width = 17
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

        if mnemonic == "Z":
            return [5 << 10]

        if mnemonic in ["A", "S", "T", "U"]:
            op = 1 if mnemonic == "A" else 2 if mnemonic == "S" else 3 if mnemonic == "T" else 4
            addr = self.parse_numeric_or_symbol(parts[1])
            return [(op << 10) | (addr & 0x3FF)]

        try:
            return [self.parse_numeric_or_symbol(line)]
        except ValueError:
            raise Exception(f"Unknown Cambridgeedsac instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled Cambridgeedsac Hex File")
        for val in values:
            out.append(f"{val & 0x1FFFF:05X}")
        return "\n".join(out) + "\n"
