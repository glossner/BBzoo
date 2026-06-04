import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class Arm1Assembler(BaseAssembler, GeneralRegisterArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 32
    address_width = 32
    num_gprs = 16
    num_fprs = 0
    op_code_width = 8
    formats = ["Single", "Double"]
    has_multiply_divide = False

    def __init__(self):
        super().__init__()
        self.comment_pattern = r'(;|//).*'

    def parse_reg(self, reg_str):
        reg_str = reg_str.strip()
        m = re.match(r'^R(\d+)$', reg_str, re.IGNORECASE)
        if m:
            val = int(m.group(1))
            if 0 <= val < 16:
                return val
        raise Exception(f"Invalid ARM1 register operand: {reg_str}")

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
        if mnemonic in ["LDR", "STR"]:
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
            # cond = 0xE, op = 0x0F
            return [(0xE << 28) | (0x0F << 20)]

        if len(parts) < 2:
            raise Exception(f"Missing operands for instruction: {line}")

        ops = parts[1].split(',')

        if mnemonic == "LDR":
            if len(ops) != 2:
                raise Exception(f"LDR instruction requires exactly 2 operands: {line}")
            rd = self.parse_reg(ops[0])
            addr = self.parse_numeric_or_symbol(ops[1])
            # cond = 0xE, op = 0x04
            inst = (0xE << 28) | (0x04 << 20) | (rd << 12)
            return [inst, addr]

        elif mnemonic == "STR":
            if len(ops) != 2:
                raise Exception(f"STR instruction requires exactly 2 operands: {line}")
            rd = self.parse_reg(ops[0])
            addr = self.parse_numeric_or_symbol(ops[1])
            # cond = 0xE, op = 0x05
            inst = (0xE << 28) | (0x05 << 20) | (rd << 12)
            return [inst, addr]

        elif mnemonic == "ADD":
            if len(ops) != 3:
                raise Exception(f"ADD instruction requires exactly 3 operands: {line}")
            rd = self.parse_reg(ops[0])
            rn = self.parse_reg(ops[1])
            rm = self.parse_reg(ops[2])
            # cond = 0xE, op = 0x00
            inst = (0xE << 28) | (0x00 << 20) | (rn << 16) | (rd << 12) | rm
            return [inst]

        else:
            raise Exception(f"Unknown ARM1 instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled ARM1 Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
