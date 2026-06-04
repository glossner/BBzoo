import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import AccumulatorArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class Intel8080AAssembler(BaseAssembler, AccumulatorArchitecture, VariableLengthFormat, FixedPointOperations):
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
        if mnemonic in ["HLT", "MOV", "ADD"]:
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

        if mnemonic == "HLT":
            return [0x76]

        if len(parts) < 2:
            raise Exception(f"Missing operand for instruction: {line}")

        operand_str = parts[1].strip()

        if mnemonic == "MOV":
            # MOV B, A
            clean_ops = operand_str.replace(" ", "").upper()
            if clean_ops == "B,A":
                return [0x47]
            else:
                raise Exception(f"Unsupported MOV operands: {operand_str}")

        elif mnemonic == "ADD":
            # ADD B
            if operand_str.upper() == "B":
                return [0x80]
            else:
                raise Exception(f"Unsupported ADD operand: {operand_str}")

        elif mnemonic in ["LDA", "STA"]:
            val = self.parse_numeric_or_symbol(operand_str)
            op = 0x3A if mnemonic == "LDA" else 0x32
            return [op, val & 0xFF, (val >> 8) & 0xFF]

        else:
            raise Exception(f"Unknown Intel 8080A instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled Intel 8080A Hex File")
        for val in values:
            out.append(f"{val & 0xFF:02X}")
        return "\n".join(out) + "\n"
