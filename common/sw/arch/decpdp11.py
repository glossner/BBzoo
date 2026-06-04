import re
from zoo_assembler import BaseAssembler

from architecture.storage_hierarchy import GeneralRegisterArchitecture
from architecture.instruction_format import VariableLengthFormat
from architecture.operations import FixedPointOperations

class Decpdp11Assembler(BaseAssembler, GeneralRegisterArchitecture, VariableLengthFormat, FixedPointOperations):
    word_width = 16
    address_width = 16
    num_gprs = 8
    num_fprs = 0
    op_code_width = 4
    formats = ["Single", "Double"]
    has_multiply_divide = False

    def __init__(self):
        super().__init__()
        self.comment_pattern = r'(;|//).*'

    def parse_operand(self, op_str):
        op_str = op_str.strip()
        if op_str.upper() == "PC":
            return 0, 7, None # Mode 0, Reg 7
        if op_str.startswith("#"):
            return 2, 7, op_str[1:] # Mode 2, Reg 7, immediate value/symbol
        
        # Autoincrement (Rn)+
        m = re.match(r'^\((R[0-7])\)\+$', op_str, re.IGNORECASE)
        if m:
            reg = int(m.group(1)[1])
            return 2, reg, None
            
        # Register Indirect (Rn)
        m = re.match(r'^\((R[0-7])\)$', op_str, re.IGNORECASE)
        if m:
            reg = int(m.group(1)[1])
            return 1, reg, None
            
        # Register Rn
        m = re.match(r'^R([0-7])$', op_str, re.IGNORECASE)
        if m:
            reg = int(m.group(1))
            return 0, reg, None
            
        raise Exception(f"Invalid PDP-11 operand: {op_str}")

    def get_arch_instruction_size(self, line):
        line = line.strip()
        if not line:
            return 0
        parts = line.split(None, 1)
        mnemonic = parts[0].upper()
        if mnemonic == "HALT":
            return 1
        
        # Double operand: MOV, ADD, SUB
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
            
        # Double operand instructions
        ops = parts[1].split(',')
        if len(ops) != 2:
            raise Exception(f"Double operand instruction requires 2 operands: {line}")
            
        src_str, dst_str = ops[0], ops[1]
        
        src_mode, src_reg, src_imm = self.parse_operand(src_str)
        dst_mode, dst_reg, dst_imm = self.parse_operand(dst_str)
        
        op_val = 0
        if mnemonic == "MOV":
            op_val = 1
        elif mnemonic == "ADD":
            op_val = 2
        elif mnemonic == "SUB":
            op_val = 3
        else:
            raise Exception(f"Unknown PDP-11 instruction: {line}")
            
        src_field = (src_mode << 3) | src_reg
        dst_field = (dst_mode << 3) | dst_reg
        inst = (op_val << 12) | (src_field << 6) | dst_field
        
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
        out.append("# Compiled DEC PDP-11 Hex File")
        for val in values:
            out.append(f"{val & 0xFFFF:04X}")
        return "\n".join(out) + "\n"
