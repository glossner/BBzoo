import re
from zoo_assembler import BaseAssembler

class ItaniumAssembler(BaseAssembler):
    def __init__(self):
        super().__init__()
        self.comment_pattern = r'(#|//).*'

    def get_arch_instruction_size(self, line):
        return 4

    def parse_numeric_or_symbol(self, val):
        val = val.strip()
        if val in self.symbols:
            return self.symbols[val]
        return self.parse_numeric(val)

    def parse_reg(self, reg_str):
        return int(reg_str.upper().replace('R', ''))

    def assemble(self, source_text):
        # We need custom assemble flow because Itanium instructions are grouped by { }
        lines = source_text.splitlines()
        stripped_lines = [l.strip() for l in lines]
        cleaned_lines = [self.clean_line(l) for l in stripped_lines]
        cleaned_lines = [l for l in cleaned_lines if l]

        # Pass 1: Build symbols (each bundle is 4 words)
        self.symbols.clear()
        self.pc = 0
        for line in cleaned_lines:
            org_match = re.match(r'^ORG\s+(.*)', line, re.IGNORECASE)
            data_match = re.match(r'^DATA\s+(.*)', line, re.IGNORECASE)
            label_match = re.match(r'^([A-Za-z_][A-Za-z0-9_]*)\s*:(.*)', line)

            if org_match:
                self.pc = self.parse_numeric(org_match.group(1))
            elif data_match:
                self.pc += len(data_match.group(1).split(','))
            elif label_match:
                label = label_match.group(1)
                self.symbols[label] = self.pc
                rest = label_match.group(2).strip()
                if rest.startswith("{"):
                    self.pc += 4
            elif line.startswith("{"):
                self.pc += 4

        # Pass 2: Assemble
        self.pc = 0
        output_values = []
        
        in_bundle = False
        bundle_template = 0
        bundle_slots = []
        stop_bits = set()

        for line in cleaned_lines:
            label_match = re.match(r'^([A-Za-z_][A-Za-z0-9_]*)\s*:(.*)', line)
            content = label_match.group(2).strip() if label_match else line

            if content:
                org_match = re.match(r'^ORG\s+(.*)', content, re.IGNORECASE)
                data_match = re.match(r'^DATA\s+(.*)', content, re.IGNORECASE)
                
                if org_match:
                    self.pc = self.parse_numeric(org_match.group(1))
                    while len(output_values) < self.pc:
                        output_values.append(0)
                elif data_match:
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
                elif content.startswith("{"):
                    in_bundle = True
                    bundle_slots = []
                    stop_bits = set()
                    
                    rest = content[1:].strip()
                    if rest:
                        if rest.startswith("."):
                            bundle_template = 1 if rest.startswith(".mmi") else 2
                        else:
                            self.parse_slot(rest, bundle_slots, stop_bits)
                elif content.startswith("}"):
                    while len(bundle_slots) < 3:
                        bundle_slots.append("nop 0")
                    
                    w0 = self.compile_itanium_slot(bundle_slots[0])
                    w1 = self.compile_itanium_slot(bundle_slots[1])
                    w2 = self.compile_itanium_slot(bundle_slots[2])
                    
                    template_word = bundle_template
                    if 0 in stop_bits: template_word |= (1 << 8)
                    if 1 in stop_bits: template_word |= (1 << 9)
                    if 2 in stop_bits: template_word |= (1 << 10)
                    
                    output_values.append(w0)
                    output_values.append(w1)
                    output_values.append(w2)
                    output_values.append(template_word)
                    
                    self.pc += 4
                    in_bundle = False
                elif in_bundle:
                    if content.startswith("."):
                        bundle_template = 1 if content.startswith(".mmi") else 2
                    else:
                        self.parse_slot(content, bundle_slots, stop_bits)

        return self.format_output(output_values)

  
    def parse_slot(self, text, slots, stop_bits):
        clean_text = text.strip()
        idx = len(slots)
        if clean_text.endswith(";;"):
            stop_bits.add(idx)
            clean_text = clean_text[:-2].strip()
        slots.append(clean_text)

    def compile_itanium_slot(self, inst_str):
        s = inst_str.strip()
        if not s or s.upper() in ["NOP 0", "NOP"]:
            return 0
        if s.upper() in ["HALT", "HLT"]:
            return 7 << 24
        
        parts = s.split(None, 1)
        op = parts[0].lower()
        if op == "jmp":
            target = self.parse_numeric_or_symbol(parts[1].strip())
            return (6 << 24) | (target & 0xFFFF)

        operands = parts[1].split('=')
        dest = operands[0].strip()
        src = operands[1].strip() if len(operands) > 1 else ""

        if op == "ld8":
            rd = self.parse_reg(dest)
            rs = self.parse_reg(src.replace('[', '').replace(']', ''))
            return (1 << 24) | (rd << 16) | (rs << 8)
        elif op == "st8":
            rd = self.parse_reg(dest.replace('[', '').replace(']', ''))
            rs = self.parse_reg(src)
            return (2 << 24) | (rd << 16) | (rs << 8)
        elif op == "ld_cu":
            rd = self.parse_reg(dest)
            target = self.parse_numeric_or_symbol(src)
            return (5 << 24) | (rd << 16) | (target & 0xFFFF)
        else:
            rd = self.parse_reg(dest)
            src_parts = [p.strip() for p in src.split(',')]
            rs1 = self.parse_reg(src_parts[0])
            rs2 = self.parse_reg(src_parts[1])
            opcode = {"add": 3, "sub": 4}[op]
            return (opcode << 24) | (rd << 16) | (rs1 << 8) | rs2

    def assemble_instruction(self, line):
        return []

    def format_output(self, values):
        out = ["# Compiled Itanium Hex File"]
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
