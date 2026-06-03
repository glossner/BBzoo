import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations, FloatingPointOperations, DecimalOperations

class Ibm360Assembler(BaseAssembler, GeneralRegisterArchitecture, VariableLengthFormat, FixedPointOperations, FloatingPointOperations, DecimalOperations):
    word_width = 32
    address_width = 24
    num_gprs = 16
    num_fprs = 4
    op_code_width = 8
    formats = ["RR", "RX", "RS", "SI", "SS"]
    has_multiply_divide = True
    fpu_precision = 64
    has_decimal_pack = True

    def clean_instruction(self, line):
        # Replace common separators like commas, parentheses with space
        line = line.replace(',', ' ').replace('(', ' ').replace(')', ' ')
        return line.split()

    def get_arch_instruction_size(self, line):
        parts = self.clean_instruction(line)
        if not parts:
            return 0
        mnemonic = parts[0].upper()
        if mnemonic == "AR":
            return 2
        elif mnemonic in ["L", "ST", "BC"]:
            return 4
        return 0

    def parse_numeric_or_symbol(self, val):
        val = val.strip()
        if val in self.symbols:
            return self.symbols[val]
        return self.parse_numeric(val)

    def assemble_instruction(self, line):
        parts = self.clean_instruction(line)
        if not parts:
            return []

        mnemonic = parts[0].upper()

        if mnemonic == "AR":
            r1 = self.parse_numeric_or_symbol(parts[1])
            r2 = self.parse_numeric_or_symbol(parts[2])
            return [0x1A, ((r1 & 0xF) << 4) | (r2 & 0xF)]

        elif mnemonic in ["L", "ST", "BC"]:
            opcodes = {
                "L": 0x58,
                "ST": 0x50,
                "BC": 0x47
            }
            opcode = opcodes[mnemonic]
            r1 = self.parse_numeric_or_symbol(parts[1])
            d2 = self.parse_numeric_or_symbol(parts[2])
            
            x2 = 0
            b2 = 0
            if len(parts) > 3:
                x2 = self.parse_numeric_or_symbol(parts[3])
            if len(parts) > 4:
                b2 = self.parse_numeric_or_symbol(parts[4])

            byte1 = ((r1 & 0xF) << 4) | (x2 & 0xF)
            byte2 = ((b2 & 0xF) << 4) | ((d2 >> 8) & 0xF)
            byte3 = d2 & 0xFF
            return [opcode, byte1, byte2, byte3]

        # Check if raw byte
        try:
            return [self.parse_numeric_or_symbol(parts[0]) & 0xFF]
        except ValueError:
            raise Exception(f"Unknown IBM 360 instruction/operand: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled IBM System/360 Hex File")
        for val in values:
            out.append(f"{val & 0xFF:02X}")
        return "\n".join(out) + "\n"
