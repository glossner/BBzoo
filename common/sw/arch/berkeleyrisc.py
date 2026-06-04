import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class BerkeleyriscAssembler(BaseAssembler, GeneralRegisterArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 32
    address_width = 32
    num_gprs = 32
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
            if 0 <= val < 32:
                return val
        raise Exception(f"Invalid Berkeley RISC-I register operand: {reg_str}")

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
        if mnemonic in ["LD", "ST"]:
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
            # op = 0x00
            return [0x00 << 24]

        if len(parts) < 2:
            raise Exception(f"Missing operands for instruction: {line}")

        ops = parts[1].split(',')

        if mnemonic == "LD":
            if len(ops) != 2:
                raise Exception(f"LD instruction requires exactly 2 operands: {line}")
            rd = self.parse_reg(ops[0])
            addr = self.parse_numeric_or_symbol(ops[1])
            # op = 0x01
            inst = (0x01 << 24) | (rd << 19)
            return [inst, addr]

        elif mnemonic == "ST":
            if len(ops) != 2:
                raise Exception(f"ST instruction requires exactly 2 operands: {line}")
            rd = self.parse_reg(ops[0])
            addr = self.parse_numeric_or_symbol(ops[1])
            # op = 0x02
            inst = (0x02 << 24) | (rd << 19)
            return [inst, addr]

        elif mnemonic == "ADD":
            if len(ops) != 3:
                raise Exception(f"ADD instruction requires exactly 3 operands: {line}")
            rd = self.parse_reg(ops[0])
            rs = self.parse_reg(ops[1])
            rm = self.parse_reg(ops[2])
            # op = 0x03
            inst = (0x03 << 24) | (rd << 19) | (rs << 14) | rm
            return [inst]

        else:
            raise Exception(f"Unknown Berkeley RISC-I instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled Berkeley RISC-I Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
