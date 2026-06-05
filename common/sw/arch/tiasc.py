import re
from zoo_assembler import BaseAssembler

class TiascAssembler(BaseAssembler):
    def get_arch_instruction_size(self, line):
        return 1

    def parse_numeric_or_symbol(self, val):
        val = val.strip()
        if val in self.symbols:
            return self.symbols[val]
        return self.parse_numeric(val)

    def parse_reg(self, reg_str):
        r = reg_str.upper().strip().replace('R', '')
        return int(r)

    def assemble_instruction(self, line):
        s = line.strip()
        if not s:
            return []

        parts = s.split(None, 1)
        op = parts[0].upper()

        if op in ["HALT", "HLT"]:
            return [3 << 28]

        if op == "JMP":
            target = self.parse_numeric_or_symbol(parts[1].strip())
            return [(4 << 28) | (target & 0xFFFF)]

        if op == "LD_CU":
            ops = [op.strip() for op in parts[1].split(',')]
            rd = self.parse_reg(ops[0])
            target = self.parse_numeric_or_symbol(ops[1])
            return [(5 << 28) | (rd << 24) | (target & 0xFFFF)]

        ops = [op.strip() for op in parts[1].split(',')]
        rc = self.parse_reg(ops[0])
        ra = self.parse_reg(ops[1])
        rb = self.parse_reg(ops[2])
        rlen = self.parse_reg(ops[3])
        opcode = {"VADD_ASC": 1, "VSUB_ASC": 2}[op]
        return [(opcode << 28) | (rc << 24) | (ra << 20) | (rb << 16) | (rlen << 12)]

    def format_output(self, values):
        out = ["# Compiled TI ASC Hex File"]
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
