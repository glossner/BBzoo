import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import FixedLengthFormat
from architecture.operations import FixedPointOperations

class Zusez1Assembler(BaseAssembler, GeneralRegisterArchitecture, FixedLengthFormat, FixedPointOperations):
    word_width = 22
    address_width = 16
    num_gprs = 2
    num_fprs = 0
    op_code_width = 8
    instruction_width = 22
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

        # Replace multiple spaces with single space
        line = re.sub(r'\s+', ' ', line)
        parts = line.split(' ')
        mnemonic = parts[0].upper()

        if mnemonic == "HLT":
            return [6 << 16]
        if mnemonic == "ADD":
            return [3 << 16]
        if mnemonic == "SUB":
            return [4 << 16]
        
        if mnemonic == "MOV":
            # MOV R1, R2
            return [5 << 16]

        if mnemonic in ["PR", "PS"]:
            op = 1 if mnemonic == "PR" else 2
            addr = self.parse_numeric_or_symbol(parts[1])
            return [(op << 16) | (addr & 0xFFFF)]

        try:
            return [self.parse_numeric_or_symbol(line)]
        except ValueError:
            raise Exception(f"Unknown Zuse Z1 instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled Zuse Z1 Hex File")
        for val in values:
            out.append(f"{val & 0x3FFFFF:06X}")
        return "\n".join(out) + "\n"
