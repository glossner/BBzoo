import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class MicronautomataAssembler(BaseAssembler, GeneralRegisterArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 32
    address_width = 32
    num_gprs = 2 # Registers R0, R1 representing active states
    num_fprs = 0
    op_code_width = 6
    formats = ["R", "I"]
    has_multiply_divide = False

    def __init__(self):
        super().__init__()
        self.comment_pattern = r'(;|//).*'

    def parse_state_reg(self, reg_str):
        reg_str = reg_str.strip()
        m = re.match(r'^R(\d+)$', reg_str, re.IGNORECASE)
        if m:
            val = int(m.group(1))
            if val in [0, 1]:
                return val
        raise Exception(f"Invalid Micron Automata state register (must be R0 or R1): {reg_str}")

    def get_arch_instruction_size(self, line):
        line = line.strip()
        if not line:
            return 0
        parts = line.split(None, 1)
        mnemonic = parts[0].upper()
        if mnemonic in ["STATE_IN", "STATE_OUT"]:
            return 2
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

        if mnemonic in ["HLT", "HALT"]:
            return [0x3F << 26]

        if len(parts) < 2:
            raise Exception(f"Missing operands for instruction: {line}")

        ops = parts[1].split(',')

        if mnemonic == "STATE_IN":
            if len(ops) != 2:
                raise Exception(f"STATE_IN requires 2 operands: {line}")
            r_in = self.parse_state_reg(ops[0])
            addr = self.parse_numeric_or_symbol(ops[1])
            return [(0x01 << 26) | (r_in << 21), addr]

        elif mnemonic == "STATE_OUT":
            if len(ops) != 2:
                raise Exception(f"STATE_OUT requires 2 operands: {line}")
            r_out = self.parse_state_reg(ops[0])
            addr = self.parse_numeric_or_symbol(ops[1])
            return [(0x03 << 26) | (r_out << 21), addr]

        elif mnemonic == "STATE_ADD":
            if len(ops) != 3:
                raise Exception(f"STATE_ADD requires 3 operands: {line}")
            r_out = self.parse_state_reg(ops[0])
            r_in1 = self.parse_state_reg(ops[1])
            r_in2 = self.parse_state_reg(ops[2])
            return [(0x02 << 26) | (r_out << 21) | (r_in1 << 16) | (r_in2 << 11)]

        else:
            raise Exception(f"Unknown Micron Automata instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled Micron Automata Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
