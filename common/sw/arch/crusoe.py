import re
from zoo_assembler import BaseAssembler

class CrusoeAssembler(BaseAssembler):
    def get_arch_instruction_size(self, line):
        return 1

    def parse_numeric_or_symbol(self, val):
        val = val.strip()
        if val in self.symbols:
            return self.symbols[val]
        return self.parse_numeric(val)

    def parse_reg(self, reg_str):
        r = reg_str.upper().strip()
        if r.startswith("R"):
            return int(r[1:])
        elif r == "EAX": return 0
        elif r == "EBX": return 1
        elif r == "ECX": return 2
        elif r == "EDX": return 3
        raise Exception(f"Invalid register: {reg_str}")

    def assemble_instruction(self, line):
        s = line.strip()
        if not s:
            return []

        parts = s.split(None, 1)
        op = parts[0].upper()

        if op in ["HALT", "HLT"]:
            return [7 << 28]

        if op == "JMP":
            target = self.parse_numeric_or_symbol(parts[1].strip())
            return [(6 << 28) | (target & 0xFFFF)]

        if op == "LD_CU":
            ops = [op.strip() for op in parts[1].split(',')]
            rd = self.parse_reg(ops[0])
            target = self.parse_numeric_or_symbol(ops[1])
            return [(5 << 28) | (rd << 24) | (target & 0xFFFF)]

        ops = [op.strip() for op in parts[1].split(',')]
        if op == "MOV":
            dest = ops[0]
            src = ops[1]
            if dest.startswith("[") and dest.endswith("]"):
                rd = self.parse_reg(dest[1:-1])
                rs = self.parse_reg(src)
                return [(2 << 28) | (rd << 24) | (rs << 20)]
            elif src.startswith("[") and src.endswith("]"):
                rd = self.parse_reg(dest)
                rs = self.parse_reg(src[1:-1])
                return [(1 << 28) | (rd << 24) | (rs << 20)]
            else:
                rd = self.parse_reg(dest)
                rs = self.parse_reg(src)
                return [(3 << 28) | (rd << 24) | (rs << 20)]
        else:
            rd = self.parse_reg(ops[0])
            rs = self.parse_reg(ops[1])
            opcode = {"ADD": 3, "SUB": 4}[op]
            return [(opcode << 28) | (rd << 24) | (rs << 20)]

    def format_output(self, values):
        out = ["# Compiled Transmeta Crusoe Hex File"]
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
