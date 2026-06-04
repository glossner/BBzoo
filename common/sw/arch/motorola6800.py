import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import AccumulatorArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class Motorola6800Assembler(BaseAssembler, AccumulatorArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 8
    address_width = 16
    has_link_bit = True
    op_code_width = 8
    has_multiply_divide = False
    formats = ["Single", "Double", "Triple"]

    def __init__(self):
        super().__init__()
        self.comment_pattern = r'(;|//).*'

    def get_arch_instruction_size(self, line):
        line = line.strip()
        if not line:
            return 0
        parts = line.split(None, 1)
        mnemonic = parts[0].upper()
        if mnemonic == "WAI":
            return 1
        return 3

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

        if mnemonic == "WAI":
            return [0x3E]

        if len(parts) < 2:
            raise Exception(f"Missing operand for instruction: {line}")

        operand_str = parts[1].strip()
        val = self.parse_numeric_or_symbol(operand_str)

        if mnemonic == "LDAA":
            return [0xB6, (val >> 8) & 0xFF, val & 0xFF] # Big-endian
        elif mnemonic == "STAA":
            return [0xB7, (val >> 8) & 0xFF, val & 0xFF] # Big-endian
        elif mnemonic == "ADDA":
            return [0xBB, (val >> 8) & 0xFF, val & 0xFF] # Big-endian
        else:
            raise Exception(f"Unknown Motorola 6800 instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled Motorola 6800 Hex File")
        for val in values:
            out.append(f"{val & 0xFF:02X}")
        return "\n".join(out) + "\n"
