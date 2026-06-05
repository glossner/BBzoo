import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class ExecubeAssembler(BaseAssembler, GeneralRegisterArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 32
    address_width = 32
    num_gprs = 4 # Registers R0-R3 represent PE0-PE3 local accumulators
    num_fprs = 0
    op_code_width = 6
    formats = ["R", "I"]
    has_multiply_divide = False

    def __init__(self):
        super().__init__()
        self.comment_pattern = r'(;|//).*'

    def parse_pe_reg(self, reg_str):
        reg_str = reg_str.strip()
        m = re.match(r'^R(\d+)$', reg_str, re.IGNORECASE)
        if m:
            val = int(m.group(1))
            if 0 <= val < 4:
                return val
        raise Exception(f"Invalid Execube register operand (must be R0-R3): {reg_str}")

    def get_arch_instruction_size(self, line):
        line = line.strip()
        if not line:
            return 0
        parts = line.split(None, 1)
        mnemonic = parts[0].upper()
        if mnemonic in ["PE_LOAD", "PE_STORE"]:
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

        if mnemonic == "PE_LOAD":
            if len(ops) != 2:
                raise Exception(f"PE_LOAD requires 2 operands: {line}")
            rd = self.parse_pe_reg(ops[0])
            addr = self.parse_numeric_or_symbol(ops[1])
            return [(0x01 << 26) | (rd << 21), addr]

        elif mnemonic == "PE_STORE":
            if len(ops) != 2:
                raise Exception(f"PE_STORE requires 2 operands: {line}")
            rd = self.parse_pe_reg(ops[0])
            addr = self.parse_numeric_or_symbol(ops[1])
            return [(0x03 << 26) | (rd << 21), addr]

        elif mnemonic == "PE_ADD":
            if len(ops) != 3:
                raise Exception(f"PE_ADD requires 3 operands: {line}")
            rd = self.parse_pe_reg(ops[0])
            rs1 = self.parse_pe_reg(ops[1])
            rs2 = self.parse_pe_reg(ops[2])
            return [(0x02 << 26) | (rd << 21) | (rs1 << 16) | (rs2 << 11)]

        else:
            raise Exception(f"Unknown Execube instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled IBM Execube Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
