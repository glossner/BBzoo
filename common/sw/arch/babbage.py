import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import StackArchitecture
from architecture.instruction_format import FixedLengthFormat
from architecture.operations import FixedPointOperations

class BabbageAssembler(BaseAssembler, StackArchitecture, FixedLengthFormat, FixedPointOperations):
    word_width = 64
    address_width = 16
    stack_depth = 16
    op_code_width = 8
    instruction_width = 64
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
            return [5 << 32]
        
        if mnemonic in ["L", "S", "ADD", "SUB"]:
            op = 1 if mnemonic == "L" else 2 if mnemonic == "S" else 3 if mnemonic == "ADD" else 4
            addr = self.parse_numeric_or_symbol(parts[1])
            return [(op << 32) | (addr & 0xFFFF)]

        try:
            return [self.parse_numeric_or_symbol(line)]
        except ValueError:
            raise Exception(f"Unknown Babbage instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled Babbage Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFFFFFFFFFF:016X}")
        return "\n".join(out) + "\n"
