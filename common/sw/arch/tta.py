import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class TtaAssembler(BaseAssembler, GeneralRegisterArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 32
    address_width = 32
    num_gprs = 8
    num_fprs = 0
    op_code_width = 6
    formats = ["Move"]
    has_multiply_divide = False

    def __init__(self):
        super().__init__()
        self.comment_pattern = r'(;|//).*'
        # Socket mappings
        self.sockets = {
            "ADD_IN1": 8,
            "ADD_IN2": 9,
            "ADD_OUT": 10,
            "LSU_ADDR": 11,
            "LSU_RDATA": 12,
            "LSU_WDATA": 13,
        }
        for i in range(8):
            self.sockets[f"R{i}"] = i

    def parse_socket(self, s_str):
        s_str = s_str.strip().upper()
        if s_str in self.sockets:
            return self.sockets[s_str]
        m = re.match(r'^R(\d+)$', s_str)
        if m:
            val = int(m.group(1))
            if val < 8:
                return val
        raise Exception(f"Invalid TTA socket: {s_str}")

    def get_arch_instruction_size(self, line):
        line = line.strip()
        if not line:
            return 0
        parts = line.split(None, 1)
        mnemonic = parts[0].upper()
        if mnemonic == "HLT" or mnemonic == "HALT":
            return 1
        if mnemonic == "MOVE_IMM":
            return 2
        if mnemonic == "MOVE":
            return 1
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

        if mnemonic == "HLT" or mnemonic == "HALT":
            return [0x3F << 26]

        if len(parts) < 2:
            raise Exception(f"Missing operands for instruction: {line}")

        ops = parts[1].split(',')

        if mnemonic == "MOVE":
            if len(ops) != 2:
                raise Exception(f"MOVE instruction requires exactly 2 operands: {line}")
            src = self.parse_socket(ops[0])
            dest = self.parse_socket(ops[1])
            inst = (0x00 << 26) | (src << 20) | (dest << 14)
            return [inst]

        elif mnemonic == "MOVE_IMM":
            if len(ops) != 2:
                raise Exception(f"MOVE_IMM instruction requires exactly 2 operands: {line}")
            imm = self.parse_numeric_or_symbol(ops[0])
            dest = self.parse_socket(ops[1])
            inst = (0x01 << 26) | (dest << 14)
            return [inst, imm]

        else:
            raise Exception(f"Unknown TTA instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled TTA Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
