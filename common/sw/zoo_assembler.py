#!/usr/bin/env python3
import sys
import os
import re
import argparse
import importlib

class BaseAssembler:
    def __init__(self):
        self.symbols = {}
        self.pc = 0
        self.comment_pattern = r'(#|;|//).*'

    def clean_line(self, line):
        # Remove comment lines starting with #
        line = re.sub(r'^\s*#.*', '', line)
        # Remove comments based on comment_pattern
        line = re.sub(self.comment_pattern, '', line)
        return line.strip()

    def parse_labels(self, lines):
        self.pc = 0
        cleaned_lines = []
        for line in lines:
            cleaned = self.clean_line(line)
            if not cleaned:
                continue
            
            # Check for label at start of line: label: [instr]
            label_match = re.match(r'^([A-Za-z_][A-Za-z0-9_]*)\s*:(.*)', cleaned)
            if label_match:
                label = label_match.group(1)
                self.symbols[label] = self.pc
                rest = label_match.group(2).strip()
                if rest:
                    cleaned_lines.append(rest)
                    self.pc += self.get_instruction_size(rest)
            else:
                # Handle ORG directive if present
                org_match = re.match(r'^ORG\s+(.*)', cleaned, re.IGNORECASE)
                if org_match:
                    self.pc = self.parse_numeric(org_match.group(1))
                    cleaned_lines.append(cleaned)
                else:
                    cleaned_lines.append(cleaned)
                    self.pc += self.get_instruction_size(cleaned)
        return cleaned_lines

    def parse_numeric(self, val):
        val = val.strip()
        is_negative = False
        if val.startswith('-'):
            is_negative = True
            val = val[1:]
        elif val.startswith('+'):
            val = val[1:]

        if val.lower().startswith('0x'):
            res = int(val[2:], 16)
        elif val.lower().startswith('0o'):
            res = int(val[2:], 8)
        elif val.lower().startswith('0b'):
            res = int(val[2:], 2)
        elif val.startswith('0') and len(val) > 1 and val.isdigit():
            res = int(val, 8)
        else:
            res = int(val)

        return -res if is_negative else res

    def get_instruction_size(self, line):
        line = line.strip()
        org_match = re.match(r'^ORG\s+(.*)', line, re.IGNORECASE)
        if org_match:
            return 0
        data_match = re.match(r'^DATA\s+(.*)', line, re.IGNORECASE)
        if data_match:
            return len(data_match.group(1).split(','))
        return self.get_arch_instruction_size(line)

    def get_arch_instruction_size(self, line):
        """Returns the size of the instruction in the machine's basic unit."""
        raise NotImplementedError()

    def assemble_instruction(self, line):
        """Translates a line of assembly into a list of integers representing the binary encoding."""
        raise NotImplementedError()

    def format_output(self, values):
        """Formats the list of output values into the target machine's hex format."""
        raise NotImplementedError()

    def assemble(self, source_text):
        lines = source_text.splitlines()
        instructions = self.parse_labels(lines)
        
        self.pc = 0
        output_values = []
        for line in instructions:
            org_match = re.match(r'^ORG\s+(.*)', line, re.IGNORECASE)
            if org_match:
                self.pc = self.parse_numeric(org_match.group(1))
                target_len = self.pc
                while len(output_values) < target_len:
                    output_values.append(0)
                continue

            data_match = re.match(r'^DATA\s+(.*)', line, re.IGNORECASE)
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
                continue

            assembled = self.assemble_instruction(line)
            output_values.extend(assembled)
            self.pc += self.get_instruction_size(line)
            
        return self.format_output(output_values)

def main():
    parser = argparse.ArgumentParser(description='Brooks Zoo Assembler')
    parser.add_argument('-arch', required=True, choices=['pdp8', 'ibm360', 'cray1', 'm68k', 'burroughsb5500', 'decpdp11', 'cdc6600', 'mos6502', 'babbage', 'harvardmark1', 'zusez1', 'manchester', 'univac1', 'ias', 'edsac', 'ibm701', 'ibm704', 'ibm650', 'ibm705', 'ibm1401'], help='Target architecture')
    parser.add_argument('input', help='Input assembly file')
    parser.add_argument('-o', required=True, help='Output hex file')
    args = parser.parse_args()

    try:
        sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
        arch_module = importlib.import_module(f'arch.{args.arch}')
        class_name = args.arch.replace('m68k', 'M68k').title() + 'Assembler'
        if args.arch == 'm68k':
            class_name = 'M68kAssembler'
        assembler_class = getattr(arch_module, class_name)
        assembler = assembler_class()
    except Exception as e:
        print(f"Error loading assembler for {args.arch}: {e}")
        sys.exit(1)

    try:
        with open(args.input, 'r') as f:
            source = f.read()
        output = assembler.assemble(source)
        with open(args.o, 'w') as f:
            f.write(output)
        print(f"Successfully assembled {args.input} to {args.o}")
    except Exception as e:
        print(f"Error assembling file: {e}")
        sys.exit(1)

    # If successfully verified, overwrite the target machine's hex file
    # (this is done in our verify flow, let's keep CLI simple).

if __name__ == '__main__':
    main()
