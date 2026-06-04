import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class IbmmwaveAssembler(BaseAssembler, GeneralRegisterArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 16
    address_width = 16
    num_gprs = 16
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
            return [0x00 << 8]
        if mnemonic == "ADD":
            return [0x32 << 8]

        if len(parts) < 2:
            raise Exception(f"Missing operands for instruction: {line}")

        ops = parts[1].split(',')

        if mnemonic == "LD":
            if len(ops) != 2:
                raise Exception(f"LD instruction requires exactly 2 operands: {line}")
            reg = ops[0].strip().upper()
            addr = self.parse_numeric_or_symbol(ops[1])
            if reg == "R1":
                return [0x30 << 8, addr]
            elif reg == "R2":
                return [0x31 << 8, addr]
            else:
                raise Exception(f"Invalid register for LD in IBM MWave: {reg}")

        elif mnemonic == "ST":
            if len(ops) != 2:
                raise Exception(f"ST instruction requires exactly 2 operands: {line}")
            reg = ops[0].strip().upper()
            addr = self.parse_numeric_or_symbol(ops[1])
            if reg == "R1":
                return [0x33 << 8, addr]
            else:
                raise Exception(f"Invalid register for ST in IBM MWave: {reg}")

        else:
            raise Exception(f"Unknown IBM MWave instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled IBM MWave Hex File")
        for val in values:
            out.append(f"{val & 0xFFFF:04X}")
        return "\n".join(out) + "\n"
