import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import StackArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class LilithAssembler(BaseAssembler, StackArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 16
    address_width = 16
    stack_depth = 16
    op_code_width = 8
    formats = ["Single", "Double"]
    has_multiply_divide = False

    def __init__(self):
        super().__init__()
        self.comment_pattern = r'(;|//).*'

    def get_arch_instruction_size(self, line):
        line = line.strip()
        if not line:
            return 0
        parts = line.split(None, 1)
        mnemonic = parts[0].upper()
        if mnemonic == "HALT":
            return 1
        if mnemonic == "ADD":
            return 1
        if mnemonic in ["PUSH", "POP"]:
            return 2
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
        parts = line.split(None, 1)
        mnemonic = parts[0].upper()

        if mnemonic == "HALT":
            return [0x00 << 8]
        if mnemonic == "ADD":
            return [0x50 << 8]

        if len(parts) < 2:
            raise Exception(f"Missing operands for instruction: {line}")

        val_str = parts[1].strip()

        if mnemonic == "PUSH":
            addr = self.parse_numeric_or_symbol(val_str)
            return [0x10 << 8, addr]

        elif mnemonic == "POP":
            addr = self.parse_numeric_or_symbol(val_str)
            return [0x30 << 8, addr]

        else:
            raise Exception(f"Unknown Lilith instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled Lilith Hex File")
        for val in values:
            out.append(f"{val & 0xFFFF:04X}")
        return "\n".join(out) + "\n"
