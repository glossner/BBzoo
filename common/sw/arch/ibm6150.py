import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class Ibm6150Assembler(BaseAssembler, GeneralRegisterArchitecture, VariableLengthFormat, FixedPointOperations):
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
            return int(m.group(1))
        raise Exception(f"Invalid register operand: {reg_str}")

    def get_arch_instruction_size(self, line):
        line = line.strip()
        if not line:
            return 0
        parts = line.split(None, 1)
        mnemonic = parts[0].upper()
        if mnemonic == "HALT":
            return 1
        if mnemonic == "A":
            return 1
        if mnemonic in ["L", "ST"]:
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
            return [0]

        if len(parts) < 2:
            raise Exception(f"Missing operands for instruction: {line}")

        ops = parts[1].split(',')
        if len(ops) != 2:
            raise Exception(f"Two operand instruction requires exactly 2 operands: {line}")

        op0_str, op1_str = ops[0].strip(), ops[1].strip()

        if mnemonic == "L":
            rx = self.parse_reg(op0_str)
            addr = self.parse_numeric_or_symbol(op1_str)
            # Opcode 0x80, RegX rx
            inst = (0x80 << 24) | (rx << 20)
            return [inst, addr]

        elif mnemonic == "ST":
            rx = self.parse_reg(op0_str)
            addr = self.parse_numeric_or_symbol(op1_str)
            # Opcode 0x90, RegX rx
            inst = (0x90 << 24) | (rx << 20)
            return [inst, addr]

        elif mnemonic == "A":
            rx = self.parse_reg(op0_str)
            ry = self.parse_reg(op1_str)
            # Opcode 0xA0, RegX rx, RegY ry
            inst = (0xA0 << 24) | (rx << 20) | (ry << 16)
            return [inst]

        else:
            raise Exception(f"Unknown IBM 6150 instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled IBM 6150 Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
