import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class DecvaxAssembler(BaseAssembler, GeneralRegisterArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 32
    address_width = 32
    num_gprs = 16
    num_fprs = 0
    op_code_width = 8
    formats = ["Single", "Double"]
    has_multiply_divide = False

    def __init__(self):
        super().__init__()
        self.comment_pattern = r'(;|//).*'

    def parse_operand(self, op_str):
        op_str = op_str.strip()
        if op_str.upper() == "PC":
            return 5, 15, None
        if op_str.upper() == "SP":
            return 5, 14, None

        if op_str.startswith("#"):
            return 8, 15, op_str[1:]
        
        # Autoincrement (Rn)+
        m = re.match(r'^\((R\d+)\)\+$', op_str, re.IGNORECASE)
        if m:
            reg = int(m.group(1)[1:])
            return 8, reg, None
            
        # Register Indirect (Rn)
        m = re.match(r'^\((R\d+)\)$', op_str, re.IGNORECASE)
        if m:
            reg = int(m.group(1)[1:])
            return 6, reg, None
            
        # Register Rn
        m = re.match(r'^R(\d+)$', op_str, re.IGNORECASE)
        if m:
            reg = int(m.group(1))
            return 5, reg, None
            
        raise Exception(f"Invalid VAX operand: {op_str}")

    def get_arch_instruction_size(self, line):
        line = line.strip()
        if not line:
            return 0
        parts = line.split(None, 1)
        mnemonic = parts[0].upper()
        if mnemonic == "HALT":
            return 1
        
        if len(parts) < 2:
            return 1
            
        ops = parts[1].split(',')
        if len(ops) != 2:
            return 1
            
        size = 1
        for op in ops:
            mode, reg, imm = self.parse_operand(op)
            if imm is not None:
                size += 1
        return size

    def assemble_instruction(self, line):
        line = line.strip()
        if not line:
            return []
            
        parts = line.split(None, 1)
        mnemonic = parts[0].upper()
        
        if mnemonic == "HALT":
            return [0]
            
        if len(parts) < 2:
            raise Exception(f"Missing operands for instruction: {line}")
            
        ops = parts[1].split(',')
        if len(ops) != 2:
            raise Exception(f"Double operand instruction requires 2 operands: {line}")
            
        src_str, dst_str = ops[0], ops[1]
        
        src_mode, src_reg, src_imm = self.parse_operand(src_str)
        dst_mode, dst_reg, dst_imm = self.parse_operand(dst_str)
        
        op_val = 0
        if mnemonic == "MOVL":
            op_val = 0xD0
        elif mnemonic == "ADDL2":
            op_val = 0xC0
        elif mnemonic == "SUBL2":
            op_val = 0xC2
        else:
            raise Exception(f"Unknown VAX instruction: {line}")
            
        src_field = (src_mode << 4) | src_reg
        dst_field = (dst_mode << 4) | dst_reg
        # Encoded word: opcode (31-24) | src_field (23-16) | dst_field (15-8) | unused (7-0)
        inst = (op_val << 24) | (src_field << 16) | (dst_field << 8)
        
        res = [inst]
        if src_imm is not None:
            if src_imm in self.symbols:
                res.append(self.symbols[src_imm])
            else:
                res.append(self.parse_numeric(src_imm))
        if dst_imm is not None:
            if dst_imm in self.symbols:
                res.append(self.symbols[dst_imm])
            else:
                res.append(self.parse_numeric(dst_imm))
        return res

    def format_output(self, values):
        out = []
        out.append("# Compiled DEC VAX Hex File")
        for val in values:
            out.append(f"{val & 0xFFFFFFFF:08X}")
        return "\n".join(out) + "\n"
