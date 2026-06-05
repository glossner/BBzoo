import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class UpmemAssembler(BaseAssembler, GeneralRegisterArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 32
    address_width = 32
    num_gprs = 24
    num_fprs = 0
    op_code_width = 6
    formats = ["R", "I"]
    has_multiply_divide = False

    def __init__(self):
        super().__init__()
        self.comment_pattern = r'(;|//).*'

    def parse_reg(self, reg_str):
        reg_str = reg_str.strip()
        m = re.match(r'^R(\d+)$', reg_str, re.IGNORECASE)
        if m:
            return int(m.group(1))
        raise Exception(f"Invalid UPMEM register operand: {reg_str}")

    def get_arch_instruction_size(self, line):
        line = line.strip()
        if not line:
            return 0
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

        if mnemonic in ["HLT", "HALT"]:
            return [0x3F << 26]

        if len(parts) < 2:
            raise Exception(f"Missing operands for instruction: {line}")

        ops = parts[1].split(',')

        if mnemonic == "LW":
            if len(ops) != 3:
                raise Exception(f"LW instruction requires 3 operands: {line}")
            rd = self.parse_reg(ops[0])
            rs = self.parse_reg(ops[1])
            offset = self.parse_numeric_or_symbol(ops[2])
            return [(0x02 << 26) | (rd << 21) | (rs << 16) | (offset & 0xFFFF)]

        elif mnemonic == "SW":
            if len(ops) != 3:
                raise Exception(f"SW instruction requires 3 operands: {line}")
            rd = self.parse_reg(ops[0])
            rs = self.parse_reg(ops[1])
            offset = self.parse_numeric_or_symbol(ops[2])
            return [(0x03 << 26) | (rd << 21) | (rs << 16) | (offset & 0xFFFF)]

        elif mnemonic == "ADD":
            if len(ops) != 3:
                raise Exception(f"ADD instruction requires 3 operands: {line}")
            rd = self.parse_reg(ops[0])
            rs1 = self.parse_reg(ops[1])
            rs2 = self.parse_reg(ops[2])
            return [(0x01 << 26) | (rd << 21) | (rs1 << 16) | (rs2 << 11)]

        elif mnemonic == "SUB":
            if len(ops) != 3:
                raise Exception(f"SUB instruction requires 3 operands: {line}")
            rd = self.parse_reg(ops[0])
            rs1 = self.parse_reg(ops[1])
            rs2 = self.parse_reg(ops[2])
            return [(0x04 << 26) | (rd << 21) | (rs1 << 16) | (rs2 << 11)]

        elif mnemonic == "ADDI":
            if len(ops) != 3:
                raise Exception(f"ADDI instruction requires 3 operands: {line}")
            rd = self.parse_reg(ops[0])
            rs1 = self.parse_reg(ops[1])
            imm = self.parse_numeric_or_symbol(ops[2])
            return [(0x05 << 26) | (rd << 21) | (rs1 << 16) | (imm & 0xFFFF)]

        elif mnemonic == "BNE":
            if len(ops) != 3:
                raise Exception(f"BNE instruction requires 3 operands: {line}")
            rs1 = self.parse_reg(ops[0])
            rs2 = self.parse_reg(ops[1])
            
            # offset can be a symbol or number
            target = self.parse_numeric_or_symbol(ops[2])
            # calculate relative displacement relative to PC + 1 (since PC updates at instruction completion)
            disp = target - (self.current_address + 1)
            return [(0x06 << 26) | (rs1 << 21) | (rs2 << 16) | (disp & 0xFFFF)]

        else:
            raise Exception(f"Unknown UPMEM instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled UPMEM Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
