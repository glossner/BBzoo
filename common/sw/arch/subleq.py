from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import StackArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class SubleqAssembler(BaseAssembler, StackArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 32
    address_width = 32
    stack_depth = 0
    op_code_width = 0
    formats = ["Subleq"]
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
        if mnemonic == "SUBLEQ":
            return 3
        if mnemonic == "HLT" or mnemonic == "HALT":
            return 3
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
            # HALT is represented in SUBLEQ as subtracting 0 from 0 and jumping to -1
            return [0, 0, 0xFFFFFFFF]

        if mnemonic == "SUBLEQ":
            if len(parts) < 2:
                raise Exception(f"Missing operands for SUBLEQ: {line}")
            ops = parts[1].split(',')
            if len(ops) != 3:
                raise Exception(f"SUBLEQ requires A, B, C operands: {line}")
            a = self.parse_numeric_or_symbol(ops[0])
            b = self.parse_numeric_or_symbol(ops[1])
            c = self.parse_numeric_or_symbol(ops[2])
            return [a, b, c]

        else:
            raise Exception(f"Unknown SUBLEQ instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled SUBLEQ Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
