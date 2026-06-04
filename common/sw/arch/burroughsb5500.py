import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import StackArchitecture
from architecture.instruction_format import FixedLengthFormat
from architecture.operations import FixedPointOperations

class Burroughsb5500Assembler(BaseAssembler, StackArchitecture, FixedLengthFormat, FixedPointOperations):
    word_width = 48
    address_width = 15
    stack_depth = 16
    op_code_width = 8
    instruction_width = 48
    has_multiply_divide = True

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
            return [5 << 40]
        elif mnemonic == "ADD":
            return [3 << 40]
        elif mnemonic == "SUB":
            return [4 << 40]
        elif mnemonic in ["PUSH", "POP"]:
            op = 1 if mnemonic == "PUSH" else 2
            addr = self.parse_numeric_or_symbol(parts[1])
            return [(op << 40) | ((addr & 0x7FFF) << 25)]

        try:
            return [self.parse_numeric_or_symbol(line)]
        except ValueError:
            raise Exception(f"Unknown B5500 instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled Burroughs B5500 Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFFFFFF:012X}")
        return "\n".join(out) + "\n"
