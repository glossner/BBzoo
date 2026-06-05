import re
from zoo_assembler import BaseAssembler

class Necsx2Assembler(BaseAssembler):
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

    def parse_vec_reg(self, reg_str):
        v = reg_str.upper().strip().replace('V', '')
        return int(v)

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

        if op == "SETVL":
            rs = self.parse_reg(parts[1].strip())
            return [(6 << 28) | (rs << 24)]

        ops = [op.strip() for op in parts[1].split(',')]
        if op == "VLD":
            vi = self.parse_vec_reg(ops[0])
            rs = self.parse_reg(ops[1])
            return [(7 << 28) | (vi << 24) | (rs << 20)]
        elif op == "VST":
            vi = self.parse_vec_reg(ops[0])
            rs = self.parse_reg(ops[1])
            return [(8 << 28) | (vi << 24) | (rs << 20)]
        elif op == "VSADD":
            vk = self.parse_vec_reg(ops[0])
            vi = self.parse_vec_reg(ops[1])
            rs = self.parse_reg(ops[2])
            return [(2 << 28) | (vk << 24) | (vi << 20) | (rs << 16)]
        elif op == "VADD":
            vk = self.parse_vec_reg(ops[0])
            vi = self.parse_vec_reg(ops[1])
            vj = self.parse_vec_reg(ops[2])
            return [(1 << 28) | (vk << 24) | (vi << 20) | (vj << 16)]
        else:
            raise Exception(f"Unsupported NEC SX-2 operation: {op}")

    def format_output(self, values):
        out = ["# Compiled NEC SX-2 Hex File"]
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
