import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class Amdr600Assembler(BaseAssembler, GeneralRegisterArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 32
    address_width = 16
    num_gprs = 4
    num_fprs = 0
    op_code_width = 8
    formats = ["Single"]
    has_multiply_divide = False

    def __init__(self):
        super().__init__()
        self.comment_pattern = r'(;|//).*'

    def get_arch_instruction_size(self, line):
        line = line.strip()
        if not line:
            return 0
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

        opA, destA, src1A, src2A = 0, 0, 0, 0
        opB, regB, addrB = 0, 0, 0

        # Split into Slots
        if '|' in line:
            parts = line.split('|', 1)
            partA = parts[0].strip()
            partB = parts[1].strip()
        else:
            part = line.strip()
            tokens = part.split(None, 1)
            mnemonic = tokens[0].upper()
            if mnemonic in ["LD", "ST"]:
                partA = "NOP"
                partB = part
            else:
                partA = part
                partB = "NOP"

        # Parse Slot A (ALU)
        if partA:
            tokensA = partA.split(None, 1)
            mnemonicA = tokensA[0].upper()
            if mnemonicA == "HALT":
                opA = 2
            elif mnemonicA == "ADD":
                opA = 1
                if len(tokensA) < 2:
                    raise Exception(f"Missing operands for ADD: {partA}")
                opsA = tokensA[1].split(',')
                if len(opsA) != 3:
                    raise Exception(f"ADD requires 3 operands: {partA}")
                destA = int(opsA[0].strip().upper()[1:])
                src1A = int(opsA[1].strip().upper()[1:])
                src2A = int(opsA[2].strip().upper()[1:])
            elif mnemonicA != "NOP":
                raise Exception(f"Unknown Slot A instruction: {partA}")

        # Parse Slot B (Memory)
        if partB:
            tokensB = partB.split(None, 1)
            mnemonicB = tokensB[0].upper()
            if mnemonicB in ["LD", "ST"]:
                opB = 1 if mnemonicB == "LD" else 2
                if len(tokensB) < 2:
                    raise Exception(f"Missing operands for {mnemonicB}: {partB}")
                opsB = tokensB[1].split(',')
                if len(opsB) != 2:
                    raise Exception(f"{mnemonicB} requires 2 operands: {partB}")
                regB = int(opsB[0].strip().upper()[1:])
                addrB = self.parse_numeric_or_symbol(opsB[1])
            elif mnemonicB != "NOP":
                raise Exception(f"Unknown Slot B instruction: {partB}")

        val = (opA << 28) | (destA << 24) | (src1A << 20) | (src2A << 16) | (opB << 12) | (regB << 8) | (addrB & 0xFF)
        return [val]

    def format_output(self, values):
        out = []
        out.append("# Compiled Amdr600 Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
