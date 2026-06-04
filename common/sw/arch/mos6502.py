import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import AccumulatorArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class Mos6502Assembler(BaseAssembler, AccumulatorArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 8
    address_width = 16
    has_link_bit = True # Carry flag
    op_code_width = 8
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
        if mnemonic in ["BRK", "CLC"]:
            return 1
        if len(parts) < 2:
            return 1
        
        op = parts[1].strip()
        if op.startswith("#"):
            return 2
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

        if mnemonic == "BRK":
            return [0x00]
        if mnemonic == "CLC":
            return [0x18]

        if len(parts) < 2:
            raise Exception(f"Missing operand for instruction: {line}")

        operand_str = parts[1].strip()
        is_imm = operand_str.startswith("#")
        val_str = operand_str[1:] if is_imm else operand_str

        val = self.parse_numeric_or_symbol(val_str)

        if mnemonic == "LDA":
            if is_imm:
                return [0xA9, val & 0xFF]
            else:
                return [0xAD, val & 0xFF, (val >> 8) & 0xFF]
        elif mnemonic == "STA":
            if is_imm:
                raise Exception("STA does not support immediate addressing mode")
            else:
                return [0x8D, val & 0xFF, (val >> 8) & 0xFF]
        elif mnemonic == "ADC":
            if is_imm:
                return [0x69, val & 0xFF]
            else:
                return [0x6D, val & 0xFF, (val >> 8) & 0xFF]
        else:
            raise Exception(f"Unknown MOS 6502 instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled MOS 6502 Hex File")
        for val in values:
            out.append(f"{val & 0xFF:02X}")
        return "\n".join(out) + "\n"
