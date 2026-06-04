import re
from zoo_assembler import BaseAssembler

class Cydra5Assembler(BaseAssembler):
    def __init__(self):
        super().__init__()
        self.comment_pattern = r'(#|//).*'

    def get_arch_instruction_size(self, line):
        return 2

    def parse_numeric_or_symbol(self, val):
        val = val.strip()
        if val in self.symbols:
            return self.symbols[val]
        return self.parse_numeric(val)

    def parse_reg(self, reg_str):
        return int(reg_str.upper().replace('R', ''))

    def assemble_instruction(self, line):
        s = line.strip()
        if not s:
            return []

        upper = s.upper()
        if upper == "HLT" or upper == "HALT":
            return [0, 2 << 20]
        if upper.startswith("BR_ROT "):
            target = self.parse_numeric_or_symbol(s[7:])
            return [0, (1 << 20) | (target & 0xFFFF)]
        if upper.startswith("LD_CU "):
            parts = s[6:].split(",")
            rd = self.parse_reg(parts[0])
            target = self.parse_numeric_or_symbol(parts[1])
            return [0, (3 << 20) | (rd << 16) | (target & 0xFFFF)]

        slots = s.split(';')
        if len(slots) != 3:
            raise Exception(f"Invalid Cydra 5 VLIW instruction: {line}")

        alu_field = 0
        lsu_field = 0
        ctrl_field = 0

        # 1. ALU
        alu_str = slots[0].strip()
        if alu_str.upper() != "NOP":
            aparts = alu_str.split(None, 1)
            aop = aparts[0].upper()
            aops = [op.strip() for op in aparts[1].split(',')]
            ard = self.parse_reg(aops[0])
            ars1 = self.parse_reg(aops[1])
            ars2 = self.parse_reg(aops[2])
            aopcode = {"ADD": 1, "SUB": 2}[aop]
            alu_field = (aopcode << 9) | (ard << 6) | (ars1 << 3) | ars2

        # 2. LSU
        lsu_str = slots[1].strip()
        if lsu_str.upper() != "NOP":
            lparts = lsu_str.split(None, 1)
            lop = lparts[0].upper()
            lops = [op.strip() for op in lparts[1].split(',')]
            lreg = self.parse_reg(lops[0])
            lbase = self.parse_reg(lops[1])
            lopcode = {"LD": 1, "ST": 2, "LD_CU": 3}[lop]
            lsu_field = (lopcode << 6) | (lreg << 3) | lbase

        # 3. CTRL
        ctrl_str = slots[2].strip()
        if ctrl_str.upper() != "NOP":
            cparts = ctrl_str.split(None, 1)
            cop = cparts[0].upper()
            copcode = {"BR_ROT": 1, "HLT": 2}[cop]
            target = self.parse_numeric_or_symbol(cparts[1].strip()) if cop == "BR_ROT" else 0
            ctrl_field = (copcode << 20) | (target & 0xFFFF)

        w0 = (alu_field << 16) | (lsu_field & 0xFFFF)
        w1 = ctrl_field
        return [w0, w1]

    def format_output(self, values):
        out = ["# Compiled Cydra 5 Hex File"]
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
