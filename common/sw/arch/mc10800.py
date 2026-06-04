from zoo_assembler import BaseAssembler
from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class Mc10800Assembler(BaseAssembler, GeneralRegisterArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 16
    address_width = 16
    num_gprs = 2
    num_fprs = 0
    op_code_width = 4
    formats = ["Single", "Double"]
    has_multiply_divide = False

    def get_arch_instruction_size(self, line):
        s = line.strip().replace("\t", " ")
        while "  " in s:
            s = s.replace("  ", " ")
        if not s:
            return 0
        parts = s.split(" ", 1)
        mnemonic = parts[0].upper()
        if mnemonic in ["HALT", "HLT"]:
            return 1
        if mnemonic in ["LD", "ST", "JMP", "JNZ"]:
            return 2
        return 1

    def parse_numeric_or_symbol(self, val):
        val = val.strip()
        if val in self.symbols:
            return self.symbols[val]
        return self.parse_numeric(val)

    def parse_reg(self, reg_str):
        s = reg_str.strip().upper()
        if s == "ACC":
            return 0
        elif s == "DR":
            return 1
        elif s.startswith("R"):
            return int(s[1:])
        else:
            raise Exception(f"Invalid register: {reg_str}")

    def assemble_instruction(self, line):
        s = line.strip().replace("\t", " ")
        while "  " in s:
            s = s.replace("  ", " ")
        if not s:
            return []
        parts = s.split(" ", 1)
        mnemonic = parts[0].upper()

        if mnemonic in ["HALT", "HLT"]:
            return [0x0000]

        ops = parts[1].split(",")

        if mnemonic == "LD":
            rd = self.parse_reg(ops[0])
            addr = self.parse_numeric_or_symbol(ops[1])
            return [0x1000 | (rd << 8), addr]

        elif mnemonic == "ST":
            rs = self.parse_reg(ops[0])
            addr = self.parse_numeric_or_symbol(ops[1])
            return [0x2000 | (rs << 8), addr]

        elif mnemonic == "ADD":
            rd = self.parse_reg(ops[0])
            ra = self.parse_reg(ops[1])
            rb = self.parse_reg(ops[2])
            return [0x3000 | (rd << 8) | (ra << 4) | rb]

        elif mnemonic == "SUB":
            rd = self.parse_reg(ops[0])
            ra = self.parse_reg(ops[1])
            rb = self.parse_reg(ops[2])
            return [0x4000 | (rd << 8) | (ra << 4) | rb]

        elif mnemonic == "AND":
            rd = self.parse_reg(ops[0])
            ra = self.parse_reg(ops[1])
            rb = self.parse_reg(ops[2])
            return [0x5000 | (rd << 8) | (ra << 4) | rb]

        elif mnemonic == "OR":
            rd = self.parse_reg(ops[0])
            ra = self.parse_reg(ops[1])
            rb = self.parse_reg(ops[2])
            return [0x6000 | (rd << 8) | (ra << 4) | rb]

        elif mnemonic == "JNZ":
            rd = self.parse_reg(ops[0])
            addr = self.parse_numeric_or_symbol(ops[1])
            return [0x7000 | (rd << 8), addr]

        elif mnemonic == "JMP":
            addr = self.parse_numeric_or_symbol(ops[0])
            return [0x8000, addr]

        elif mnemonic == "LDI":
            rd = self.parse_reg(ops[0])
            rs = self.parse_reg(ops[1])
            return [0x9000 | (rd << 8) | (rs << 4)]

        elif mnemonic == "STI":
            rs = self.parse_reg(ops[0])
            rd = self.parse_reg(ops[1])
            return [0xA000 | (rs << 8) | (rd << 4)]

        else:
            raise Exception(f"Unknown MC10800 instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled Motorola 10800 Hex File")
        for val in values:
            out.append(f"{val & 0xFFFF:04X}")
        return "\n".join(out) + "\n"
