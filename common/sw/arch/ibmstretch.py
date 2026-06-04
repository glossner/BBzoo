import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import AccumulatorArchitecture
from architecture.instruction_format import FixedLengthFormat
from architecture.operations import FixedPointOperations

class IbmstretchAssembler(BaseAssembler, AccumulatorArchitecture, FixedLengthFormat, FixedPointOperations):
    word_width = 64
    address_width = 20
    has_link_bit = False
    op_code_width = 8
    instruction_width = 64
    has_multiply_divide = False

    def get_arch_instruction_size(self, line):
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

        if mnemonic == "HLT":
            return [6 << 56]

        rest = parts[1].strip() if len(parts) > 1 else ""

        if mnemonic in ["LD", "ADD", "ST"]:
            op = 1 if mnemonic == "LD" else 2 if mnemonic == "ADD" else 3
            # Matches: ACC, addr(Xreg) or ACC, addr
            match = re.match(r'^ACC,\s*([A-Za-z0-9_]+)(?:\((X[0-9]+)\))?$', rest, re.IGNORECASE)
            if not match:
                raise Exception(f"Invalid IBM Stretch accumulator instruction format: {line}")
            
            addr_str = match.group(1)
            xreg_str = match.group(2)
            
            addr = self.parse_numeric_or_symbol(addr_str)
            xreg = 0
            if xreg_str:
                xreg = int(xreg_str[1:])
            
            return [(op << 56) | ((xreg & 15) << 52) | (addr & 0xFFFFF)]

        if mnemonic in ["LDX", "ADDX"]:
            op = 4 if mnemonic == "LDX" else 5
            # Matches: Xreg, value
            match = re.match(r'^(X[0-9]+),\s*([A-Za-z0-9_]+)$', rest, re.IGNORECASE)
            if not match:
                raise Exception(f"Invalid IBM Stretch index instruction format: {line}")
            
            xreg_str = match.group(1)
            val_str = match.group(2)
            
            xreg = int(xreg_str[1:])
            val = self.parse_numeric_or_symbol(val_str)
            
            return [(op << 56) | ((xreg & 15) << 52) | (val & 0xFFFFF)]

        try:
            return [self.parse_numeric_or_symbol(line)]
        except ValueError:
            raise Exception(f"Unknown IBM Stretch instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled IBM Stretch Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFFFFFFFFFF:016X}")
        return "\n".join(out) + "\n"
