import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class Powervr1Assembler(BaseAssembler, GeneralRegisterArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 32
    address_width = 16
    num_gprs = 4
    num_fprs = 0
    op_code_width = 8
    formats = ["Single", "Double"]
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
        if mnemonic in ["HALT", "HSR"]:
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
            return [0x00 << 24]

        if len(parts) < 2:
            raise Exception(f"Missing operands for instruction: {line}")

        ops = parts[1].split(',')

        if mnemonic == "LD":
            if len(ops) != 2:
                raise Exception(f"LD instruction requires exactly 2 operands: {line}")
            reg = ops[0].strip().upper()
            addr = self.parse_numeric_or_symbol(ops[1])
            reg_idx = int(reg[1:])
            if reg_idx < 0 or reg_idx >= 4:
                raise Exception(f"Invalid register index: {reg}")
            return [(0x10 << 24) | (reg_idx << 22), addr]

        elif mnemonic == "ST":
            if len(ops) != 2:
                raise Exception(f"ST instruction requires exactly 2 operands: {line}")
            reg = ops[0].strip().upper()
            addr = self.parse_numeric_or_symbol(ops[1])
            reg_idx = int(reg[1:])
            if reg_idx < 0 or reg_idx >= 4:
                raise Exception(f"Invalid register index: {reg}")
            return [(0x30 << 24) | (reg_idx << 22), addr]

        elif mnemonic == "HSR":
            if len(ops) != 4:
                raise Exception(f"HSR instruction requires exactly 4 operands: {line}")
            dest = ops[0].strip().upper()
            src1 = ops[1].strip().upper()
            src2 = ops[2].strip().upper()
            depth = ops[3].strip().upper()
            
            dest_idx = int(dest[1:])
            src1_idx = int(src1[1:])
            src2_idx = int(src2[1:])
            depth_idx = int(depth[1:])
            
            if any(idx < 0 or idx >= 4 for idx in [dest_idx, src1_idx, src2_idx, depth_idx]):
                raise Exception(f"Invalid register index in HSR: {line}")
            return [(0x20 << 24) | (dest_idx << 22) | (src1_idx << 18) | (src2_idx << 14) | (depth_idx << 10)]

        else:
            raise Exception(f"Unknown Powervr1 instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled Powervr1 Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
