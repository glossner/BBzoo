import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class SamsungpimAssembler(BaseAssembler, GeneralRegisterArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 32
    address_width = 32
    num_gprs = 4 # Vector registers V0-V3
    num_fprs = 0
    op_code_width = 6
    formats = ["R", "I"]
    has_multiply_divide = False

    def __init__(self):
        super().__init__()
        self.comment_pattern = r'(;|//).*'

    def parse_vector_reg(self, reg_str):
        reg_str = reg_str.strip()
        m = re.match(r'^V(\d+)$', reg_str, re.IGNORECASE)
        if m:
            return int(m.group(1))
        raise Exception(f"Invalid Samsung PIM vector register: {reg_str}")

    def get_arch_instruction_size(self, line):
        line = line.strip()
        if not line:
            return 0
        parts = line.split(None, 1)
        mnemonic = parts[0].upper()
        if mnemonic in ["PCU_LD", "PCU_ST"]:
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

        if mnemonic in ["HLT", "HALT"]:
            return [0x3F << 26]

        if len(parts) < 2:
            raise Exception(f"Missing operands for instruction: {line}")

        ops = parts[1].split(',')

        if mnemonic == "PCU_LD":
            if len(ops) != 2:
                raise Exception(f"PCU_LD requires 2 operands: {line}")
            vd = self.parse_vector_reg(ops[0])
            addr = self.parse_numeric_or_symbol(ops[1])
            return [(0x01 << 26) | (vd << 21), addr]

        elif mnemonic == "PCU_ST":
            if len(ops) != 2:
                raise Exception(f"PCU_ST requires 2 operands: {line}")
            vs = self.parse_vector_reg(ops[0])
            addr = self.parse_numeric_or_symbol(ops[1])
            return [(0x02 << 26) | (vs << 21), addr]

        elif mnemonic == "PCU_ADD":
            if len(ops) != 3:
                raise Exception(f"PCU_ADD requires 3 operands: {line}")
            vd = self.parse_vector_reg(ops[0])
            va = self.parse_vector_reg(ops[1])
            vb = self.parse_vector_reg(ops[2])
            return [(0x03 << 26) | (vd << 21) | (va << 16) | (vb << 11)]

        else:
            raise Exception(f"Unknown Samsung PIM instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled Samsung HBM2-PIM Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
