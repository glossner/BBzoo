import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import FixedLengthFormat
from architecture.operations import FixedPointOperations

class Harvardmark1Assembler(BaseAssembler, GeneralRegisterArchitecture, FixedLengthFormat, FixedPointOperations):
    word_width = 64
    address_width = 8
    num_gprs = 72
    num_fprs = 0
    op_code_width = 8
    instruction_width = 64
    has_multiply_divide = False

    def get_arch_instruction_size(self, line):
        return 1

    def parse_register(self, reg_str):
        reg_str = reg_str.strip().upper()
        if reg_str.startswith("R"):
            return int(reg_str[1:])
        raise Exception(f"Invalid Harvard Mark I register: {reg_str}")

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

        if mnemonic == "HLT":
            return [4 << 16]

        if len(parts) < 2:
            raise Exception(f"Missing operands for instruction: {line}")

        ops = parts[1].split(',')
        src_str = ops[0].strip()
        dst_str = ops[1].strip()

        op = 1 if mnemonic == "MOV" else 2 if mnemonic == "ADD" else 3
        src = self.parse_register(src_str)
        dst = self.parse_register(dst_str)

        return [(op << 16) | (src << 8) | dst]

    def format_output(self, values):
        out = []
        out.append("# Compiled Harvard Mark I Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFFFFFFFFFF:016X}")
        return "\n".join(out) + "\n"
