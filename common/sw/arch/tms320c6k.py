import re
from zoo_assembler import BaseAssembler

class Tms320C6KAssembler(BaseAssembler):
    def __init__(self):
        super().__init__()
        self.parallel_pcs = set()

    def get_arch_instruction_size(self, line):
        return 1

    def parse_numeric_or_symbol(self, val):
        val = val.strip()
        if val in self.symbols:
            return self.symbols[val]
        return self.parse_numeric(val)

    def parse_reg(self, reg_str):
        return int(reg_str.upper().replace('R', ''))

    def assemble(self, source_text):
        self.parallel_pcs.clear()
        lines = source_text.splitlines()
        
        # Strip "||" for label parsing to resolve addresses correctly
        stripped_lines = []
        for line in lines:
            t = line.strip()
            if t.startswith("||"):
                stripped_lines.append(t[2:].strip())
            else:
                stripped_lines.append(t)
                
        self.symbols.clear()
        self.parse_labels(stripped_lines)

        # Pre-scan lines to find parallel groupings
        current_pc = 0
        last_inst_pc = -1
        for line in lines:
            cleaned = self.clean_line(line)
            if cleaned:
                is_parallel = cleaned.startswith("||")
                stripped = cleaned[2:].strip() if is_parallel else cleaned
                
                org_match = re.match(r'^ORG\s+(.*)', stripped, re.IGNORECASE)
                data_match = re.match(r'^DATA\s+(.*)', stripped, re.IGNORECASE)
                
                if org_match:
                    current_pc = self.parse_numeric(org_match.group(1))
                elif data_match:
                    current_pc += len(data_match.group(1).split(','))
                else:
                    if is_parallel and last_inst_pc >= 0:
                        self.parallel_pcs.add(last_inst_pc)
                    last_inst_pc = current_pc
                    current_pc += 1

        # Now assemble stripped lines and apply p-bits
        self.pc = 0
        output_values = []
        for line in stripped_lines:
            cleaned = self.clean_line(line)
            if cleaned:
                label_match = re.match(r'^([A-Za-z_][A-Za-z0-9_]*)\s*:(.*)', cleaned)
                if label_match:
                    cleaned = label_match.group(2).strip()
                if not cleaned:
                    continue
                org_match = re.match(r'^ORG\s+(.*)', cleaned, re.IGNORECASE)
                if org_match:
                    self.pc = self.parse_numeric(org_match.group(1))
                    while len(output_values) < self.pc:
                        output_values.append(0)
                else:
                    data_match = re.match(r'^DATA\s+(.*)', cleaned, re.IGNORECASE)
                    if data_match:
                        val_str = data_match.group(1)
                        vals = []
                        for v in val_str.split(','):
                            v_strip = v.strip()
                            if v_strip in self.symbols:
                                vals.append(self.symbols[v_strip])
                            else:
                                vals.append(self.parse_numeric(v_strip))
                        output_values.extend(vals)
                        self.pc += len(vals)
                    else:
                        assembled = self.assemble_instruction(cleaned)
                        updated = []
                        for inst in assembled:
                            if self.pc in self.parallel_pcs:
                                updated.append(inst | 1)
                            else:
                                updated.append(inst & ~1)
                        output_values.extend(updated)
                        self.pc += 1
                        
        return self.format_output(output_values)

    def assemble_instruction(self, line):
        s = line.strip()
        if not s:
            return []

        parts = s.split(None, 1)
        op = parts[0].upper()

        if op == "HLT":
            return [5 << 28]

        if op == "JMP":
            target = self.parse_numeric_or_symbol(parts[1].strip())
            return [(6 << 28) | ((target & 0xFFFF) << 1)]

        if op == "LD_CU":
            ops = [op.strip() for op in parts[1].split(',')]
            rd = self.parse_reg(ops[0])
            target = self.parse_numeric_or_symbol(ops[1])
            return [(7 << 28) | (rd << 24) | ((target & 0xFFFF) << 1)]

        ops = [op.strip() for op in parts[1].split(',')]
        opcode = {"LDW": 1, "STW": 2, "ADD": 3, "SUB": 4}[op]

        if op == "LDW":
            rs = self.parse_reg(ops[0].replace('*', ''))
            rd = self.parse_reg(ops[1])
            return [(opcode << 28) | (rd << 24) | (rs << 20)]
        elif op == "STW":
            rs = self.parse_reg(ops[0])
            rd = self.parse_reg(ops[1].replace('*', ''))
            return [(opcode << 28) | (rs << 24) | (rd << 20)]
        else:
            rs1 = self.parse_reg(ops[0])
            rs2 = self.parse_reg(ops[1])
            rd = self.parse_reg(ops[2])
            return [(opcode << 28) | (rd << 24) | (rs1 << 20) | (rs2 << 16)]

    def format_output(self, values):
        out = ["# Compiled TMS320C6k Hex File"]
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
