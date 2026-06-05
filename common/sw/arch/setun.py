from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import StackArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class SetunAssembler(BaseAssembler, StackArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 32
    address_width = 32
    stack_depth = 16
    op_code_width = 8
    formats = ["Stack"]
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
        if mnemonic == "HLT" or mnemonic == "HALT":
            return 1
        if mnemonic == "ADD":
            return 1
        if mnemonic in ["LOAD", "STORE"]:
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

        if mnemonic == "HLT" or mnemonic == "HALT":
            return [0xFF << 24]

        if mnemonic == "ADD":
            return [0x12 << 24]

        if len(parts) < 2:
            raise Exception(f"Missing operands for instruction: {line}")

        target = self.parse_numeric_or_symbol(parts[1])

        if mnemonic == "LOAD":
            return [0x10 << 24, target]

        elif mnemonic == "STORE":
            return [0x11 << 24, target]

        else:
            raise Exception(f"Unknown Setun instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled Setun Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
