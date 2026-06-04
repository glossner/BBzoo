import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import AccumulatorArchitecture
from architecture.instruction_format import FixedLengthFormat
from architecture.operations import FixedPointOperations

class ManchesterAssembler(BaseAssembler, AccumulatorArchitecture, FixedLengthFormat, FixedPointOperations):
    word_width = 32
    address_width = 13
    has_link_bit = False
    op_code_width = 3
    instruction_width = 32
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

        if mnemonic == "STP":
            return [(7 << 13)]

        if mnemonic in ["JMP", "JPR", "LDN", "STO", "SUB"]:
            op = 0 if mnemonic == "JMP" else 1 if mnemonic == "JPR" else 2 if mnemonic == "LDN" else 3 if mnemonic == "STO" else 4
            addr = self.parse_numeric_or_symbol(parts[1])
            return [(op << 13) | (addr & 0x1FFF)]

        try:
            return [self.parse_numeric_or_symbol(line)]
        except ValueError:
            raise Exception(f"Unknown Manchester instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled Manchester Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
