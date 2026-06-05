import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class SparcAssembler(BaseAssembler, GeneralRegisterArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 32
    address_width = 32
    num_gprs = 32
    num_fprs = 0
    op_code_width = 6
    formats = ["R", "I"]
    has_multiply_divide = False

    def __init__(self):
        super().__init__()
        self.comment_pattern = r'(;|//).*'

    def parse_reg(self, reg_str):
        reg_str = reg_str.strip().lower()
        if reg_str.startswith('%'):
            reg_str = reg_str[1:]
        
        if reg_str.startswith('g'):
            val = int(reg_str[1:])
            if 0 <= val <= 7: return val
        elif reg_str.startswith('o'):
            val = int(reg_str[1:])
            if 0 <= val <= 7: return 8 + val
        elif reg_str.startswith('l'):
            val = int(reg_str[1:])
            if 0 <= val <= 7: return 16 + val
        elif reg_str.startswith('i'):
            val = int(reg_str[1:])
            if 0 <= val <= 7: return 24 + val
        elif reg_str.startswith('r'):
            val = int(reg_str[1:])
            if 0 <= val <= 31: return val
            
        raise Exception(f"Invalid SPARC register operand: {reg_str}")

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

        if mnemonic == "HLT" or mnemonic == "HALT":
            return [0x3F << 26]

        if len(parts) < 2:
            raise Exception(f"Missing operands for instruction: {line}")

        ops = parts[1].split(',')

        if mnemonic == "LD":
            if len(ops) != 2:
                raise Exception(f"LD instruction requires exactly 2 operands: {line}")
            addr_str = ops[0].strip().strip('[]')
            addr = self.parse_numeric_or_symbol(addr_str)
            rd = self.parse_reg(ops[1])
            inst = (0x02 << 26) | (rd << 16)
            return [inst, addr]

        elif mnemonic == "ST":
            if len(ops) != 2:
                raise Exception(f"ST instruction requires exactly 2 operands: {line}")
            rs = self.parse_reg(ops[0])
            addr_str = ops[1].strip().strip('[]')
            addr = self.parse_numeric_or_symbol(addr_str)
            inst = (0x03 << 26) | (rs << 16)
            return [inst, addr]

        elif mnemonic == "ADD":
            if len(ops) != 3:
                raise Exception(f"ADD instruction requires exactly 3 operands: {line}")
            rs1 = self.parse_reg(ops[0])
            rs2 = self.parse_reg(ops[1])
            rd = self.parse_reg(ops[2])
            inst = (0x01 << 26) | (rd << 21) | (rs1 << 16) | (rs2 << 11)
            return [inst]

        else:
            raise Exception(f"Unknown SPARC instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled SPARC Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
