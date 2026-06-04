import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class Mips1Assembler(BaseAssembler, GeneralRegisterArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 32
    address_width = 32
    num_gprs = 32
    num_fprs = 0
    op_code_width = 6
    formats = ["R", "I", "J"]
    has_multiply_divide = False

    def __init__(self):
        super().__init__()
        self.comment_pattern = r'(;|//).*'

    def parse_reg(self, reg_str):
        reg_str = reg_str.strip()
        m = re.match(r'^R(\d+)$', reg_str, re.IGNORECASE)
        if m:
            return int(m.group(1))
        raise Exception(f"Invalid MIPS register operand: {reg_str}")

    def get_arch_instruction_size(self, line):
        line = line.strip()
        if not line:
            return 0
        parts = line.split(None, 1)
        mnemonic = parts[0].upper()
        if mnemonic == "HALT":
            return 1
        if mnemonic == "ADDU":
            return 1
        if mnemonic in ["LW", "SW"]:
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
            return [0x3F << 26]

        if len(parts) < 2:
            raise Exception(f"Missing operands for instruction: {line}")

        ops = parts[1].split(',')

        if mnemonic == "LW":
            if len(ops) != 2:
                raise Exception(f"LW instruction requires exactly 2 operands: {line}")
            rt = self.parse_reg(ops[0])
            addr = self.parse_numeric_or_symbol(ops[1])
            inst = (0x23 << 26) | (rt << 16)
            return [inst, addr]

        elif mnemonic == "SW":
            if len(ops) != 2:
                raise Exception(f"SW instruction requires exactly 2 operands: {line}")
            rt = self.parse_reg(ops[0])
            addr = self.parse_numeric_or_symbol(ops[1])
            inst = (0x2B << 26) | (rt << 16)
            return [inst, addr]

        elif mnemonic == "ADDU":
            if len(ops) != 3:
                raise Exception(f"ADDU instruction requires exactly 3 operands: {line}")
            rd = self.parse_reg(ops[0])
            rs = self.parse_reg(ops[1])
            rt = self.parse_reg(ops[2])
            inst = (0x00 << 26) | (rs << 21) | (rt << 16) | (rd << 11) | 0x21
            return [inst]

        else:
            raise Exception(f"Unknown MIPS I instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled MIPS I Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
