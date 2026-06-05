from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import StackArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class MitDataflowAssembler(BaseAssembler, StackArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 32
    address_width = 32
    stack_depth = 16
    op_code_width = 8
    formats = ["Dataflow"]
    has_multiply_divide = False

    def __init__(self):
        super().__init__()
        self.comment_pattern = r'(;|//).*'

    def get_arch_instruction_size(self, line):
        line = line.strip()
        if not line:
            return 0
        parts = line.split(None, 1)
        mnemonic = parts[0].upper()
        if mnemonic == "ND_HALT" or mnemonic == "HALT":
            return 1
        if mnemonic == "ND_ADD":
            return 1
        if mnemonic in ["ND_LOAD", "ND_STORE"]:
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

        if mnemonic == "ND_HALT" or mnemonic == "HALT":
            return [0xFF << 24]

        if len(parts) < 2:
            raise Exception(f"Missing operands for instruction: {line}")

        ops = parts[1].split(',')

        if mnemonic == "ND_LOAD":
            if len(ops) != 3:
                raise Exception(f"ND_LOAD requires NodeID, Slot, MemoryAddress: {line}")
            node_id = int(ops[0].strip())
            slot = int(ops[1].strip())
            addr = self.parse_numeric_or_symbol(ops[2])
            inst = (0x40 << 24) | (node_id << 16) | (slot << 8)
            return [inst, addr]

        elif mnemonic == "ND_STORE":
            if len(ops) != 2:
                raise Exception(f"ND_STORE requires Slot, MemoryAddress: {line}")
            slot = int(ops[0].strip())
            addr = self.parse_numeric_or_symbol(ops[1])
            inst = (0x42 << 24) | (slot << 8)
            return [inst, addr]

        elif mnemonic == "ND_ADD":
            if len(ops) != 1:
                raise Exception(f"ND_ADD requires TargetNodeID: {line}")
            node_id = int(ops[0].strip())
            inst = (0x41 << 24) | (node_id << 16)
            return [inst]

        else:
            raise Exception(f"Unknown MIT Dataflow instruction: {line}")

    def format_output(self, values):
        out = []
        out.append("# Compiled MIT Dataflow Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
