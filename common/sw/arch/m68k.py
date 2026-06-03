import re
from zoo_assembler import BaseAssembler

class M68kAssembler(BaseAssembler):
    def __init__(self):
        super().__init__()
        # Exclude '#' so we don't strip M68k immediate operands!
        self.comment_pattern = r'(;|//).*'

    def get_arch_instruction_size(self, line):
        line = line.strip()
        if not line:
            return 0
        if re.match(r'^MOVEA?\.L\s+#', line, re.IGNORECASE):
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

        # Replace double spaces
        line = re.sub(r'\s+', ' ', line)

        # 1. MOVEA.L #imm, An
        movea_match = re.match(r'^MOVEA\.L\s+#([^,\s]+)\s*,\s*A(\d+)', line, re.IGNORECASE)
        if movea_match:
            imm_val = self.parse_numeric_or_symbol(movea_match.group(1))
            if movea_match.group(1) in self.symbols:
                imm_val = imm_val * 2
            n = int(movea_match.group(2))
            opcode = 0x207C | (n << 9)
            return [opcode, (imm_val >> 16) & 0xFFFF, imm_val & 0xFFFF]

        # 2. MOVE.L #imm, Dn
        moveli_match = re.match(r'^MOVE\.L\s+#([^,\s]+)\s*,\s*D(\d+)', line, re.IGNORECASE)
        if moveli_match:
            imm_val = self.parse_numeric_or_symbol(moveli_match.group(1))
            if moveli_match.group(1) in self.symbols:
                imm_val = imm_val * 2
            n = int(moveli_match.group(2))
            opcode = 0x203C | (n << 9)
            return [opcode, (imm_val >> 16) & 0xFFFF, imm_val & 0xFFFF]

        # 3. MOVE.L (Am), Dn
        moveld_match = re.match(r'^MOVE\.L\s*\(A(\d+)\)\s*,\s*D(\d+)', line, re.IGNORECASE)
        if moveld_match:
            m = int(moveld_match.group(1))
            n = int(moveld_match.group(2))
            return [0x2010 | (n << 9) | m]

        # 4. MOVE.L Dn, (Am)
        movest_match = re.match(r'^MOVE\.L\s*D(\d+)\s*,\s*\(A(\d+)\)', line, re.IGNORECASE)
        if movest_match:
            n = int(movest_match.group(1))
            m = int(movest_match.group(2))
            return [0x2080 | (m << 9) | n]

        # 5. MOVE.L Dm, Dn
        moverr_match = re.match(r'^MOVE\.L\s*D(\d+)\s*,\s*D(\d+)', line, re.IGNORECASE)
        if moverr_match:
            m = int(moverr_match.group(1))
            n = int(moverr_match.group(2))
            return [0x2000 | (n << 9) | m]

        # 6. ADD.L Dm, Dn
        add_match = re.match(r'^ADD\.L\s*D(\d+)\s*,\s*D(\d+)', line, re.IGNORECASE)
        if add_match:
            m = int(add_match.group(1))
            n = int(add_match.group(2))
            return [0xD080 | (n << 9) | m]

        # 7. SUB.L Dm, Dn
        sub_match = re.match(r'^SUB\.L\s*D(\d+)\s*,\s*D(\d+)', line, re.IGNORECASE)
        if sub_match:
            m = int(sub_match.group(1))
            n = int(sub_match.group(2))
            return [0x9080 | (n << 9) | m]

        # 8. BRA target
        bra_match = re.match(r'^BRA\s+(\S+)', line, re.IGNORECASE)
        if bra_match:
            target = bra_match.group(1)
            target_pc = self.parse_numeric_or_symbol(target)
            offset = (target_pc - (self.pc + 1)) * 2
            return [0x6000 | (offset & 0xFF)]

        # Check if raw value
        try:
            return [self.parse_numeric_or_symbol(line)]
        except ValueError:
            raise Exception(f"Unknown Motorola 68000 instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled Motorola 68000 Hex File")
        for val in values:
            out.append(f"{val & 0xFFFF:04X}")
        return "\n".join(out) + "\n"
