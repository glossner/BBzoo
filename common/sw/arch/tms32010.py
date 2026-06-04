import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import AccumulatorArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class Tms32010Assembler(BaseAssembler, AccumulatorArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 16
    address_width = 16
    has_link_bit = False
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
        if mnemonic in ["HALT", "HLT"]:
            return 1
        if mnemonic in ["LAC", "ADD", "SACL"]:
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

        if mnemonic in ["HALT", "HLT"]:
            return [0x00 << 8]

        if len(parts) < 2:
            raise Exception(f"Missing operands for instruction: {line}")

        val_str = parts[1].strip()

        if mnemonic == "LAC":
            addr = self.parse_numeric_or_symbol(val_str)
            return [0x01 << 8, addr]

        elif mnemonic == "ADD":
            addr = self.parse_numeric_or_symbol(val_str)
            return [0x02 << 8, addr]

        elif mnemonic == "SACL":
            addr = self.parse_numeric_or_symbol(val_str)
            return [0x03 << 8, addr]

        else:
            raise Exception(f"Unknown TMS32010 instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled TMS32010 Hex File")
        for val in values:
            out.append(f"{val & 0xFFFF:04X}")
        return "\n".join(out) + "\n"
