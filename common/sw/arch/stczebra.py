import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import AccumulatorArchitecture
from architecture.instruction_format import FixedLengthFormat
from architecture.operations import FixedPointOperations

class StczebraAssembler(BaseAssembler, AccumulatorArchitecture, FixedLengthFormat, FixedPointOperations):
    word_width = 33
    address_width = 16
    has_link_bit = False
    op_code_width = 15
    instruction_width = 33
    has_multiply_divide = False

    def get_arch_instruction_size(self, line):
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

        parts = line.split()
        mnemonic = parts[0].upper()

        if mnemonic == "HLT":
            return [1 << 28]

        if mnemonic in ["LD", "ADD", "ST"]:
            addr_str = parts[1].strip()
            addr = self.parse_numeric_or_symbol(addr_str)
            if mnemonic == "LD":
                # C (32), R (31), A (29) bits set
                val = (1 << 32) | (1 << 31) | (1 << 29) | ((addr & 0x1FFF) << 5)
            elif mnemonic == "ADD":
                # R (31), A (29) bits set
                val = (1 << 31) | (1 << 29) | ((addr & 0x1FFF) << 5)
            else:
                # W (30) bit set
                val = (1 << 30) | ((addr & 0x1FFF) << 5)
            return [val]

        try:
            return [self.parse_numeric_or_symbol(line)]
        except ValueError:
            raise Exception(f"Unknown STC ZEBRA instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled STC ZEBRA Hex File")
        for val in values:
            out.append(f"{val & 0x1FFFFFFFF:09X}")
        return "\n".join(out) + "\n"
