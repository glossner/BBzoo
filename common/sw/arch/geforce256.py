import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class Geforce256Assembler(BaseAssembler, GeneralRegisterArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 32
    address_width = 16
    num_gprs = 4
    num_fprs = 0
    op_code_width = 8
    formats = ["Single", "Double"]
    has_multiply_divide = True

    def __init__(self):
        super().__init__()
        self.comment_pattern = r'(;|//).*'

    def get_arch_instruction_size(self, line):
        line = line.strip()
        if not line:
            return 0
        parts = line.split(None, 1)
        mnemonic = parts[0].upper()
        if mnemonic in ["HALT", "COMBINE"]:
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

        elif mnemonic == "COMBINE":
            if len(ops) != 5:
                raise Exception(f"COMBINE instruction requires exactly 5 operands: {line}")
            dest = ops[0].strip().upper()
            srcA = ops[1].strip().upper()
            srcB = ops[2].strip().upper()
            srcC = ops[3].strip().upper()
            srcD = ops[4].strip().upper()
            
            dest_idx = int(dest[1:])
            srcA_idx = int(srcA[1:])
            srcB_idx = int(srcB[1:])
            srcC_idx = int(srcC[1:])
            srcD_idx = int(srcD[1:])
            
            if any(idx < 0 or idx >= 4 for idx in [dest_idx, srcA_idx, srcB_idx, srcC_idx, srcD_idx]):
                raise Exception(f"Invalid register index in COMBINE: {line}")
            return [(0x20 << 24) | (dest_idx << 22) | (srcA_idx << 18) | (srcB_idx << 14) | (srcC_idx << 10) | (srcD_idx << 6)]

        else:
            raise Exception(f"Unknown Geforce256 instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled Geforce256 Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
