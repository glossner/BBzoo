#!/usr/bin/env python3
import sys
import os
import ast
import argparse

class Pdp8Codegen(ast.NodeVisitor):
    def __init__(self):
        self.instructions = []
        self.variables = set()
        self.constants = {}  # value -> label

    def get_const_label(self, val):
        if val not in self.constants:
            lbl = f"const_{abs(val)}"
            self.constants[val] = lbl
        return self.constants[val]

    def visit_Assign(self, node):
        if len(node.targets) != 1:
            raise ValueError("Only single-target assignments are supported.")
        target = node.targets[0]
        if not isinstance(target, ast.Name):
            raise ValueError("Target of assignment must be a variable name.")
        
        var_name = target.id
        self.variables.add(var_name)
        
        val = node.value
        if isinstance(val, ast.Constant):
            # var = 5
            c_val = val.value
            c_lbl = self.get_const_label(c_val)
            self.instructions.append("CLA")
            self.instructions.append(f"TAD {c_lbl}")
            self.instructions.append(f"DCA {var_name}")
        elif isinstance(val, ast.Name):
            # var1 = var2
            self.instructions.append("CLA")
            self.instructions.append(f"TAD {val.id}")
            self.instructions.append(f"DCA {var_name}")
        elif isinstance(val, ast.BinOp):
            # var1 = var2 + var3
            left = val.left
            right = val.right
            op = val.op
            
            if not isinstance(left, ast.Name) or not isinstance(right, ast.Name):
                raise ValueError("Binary operations are only supported between variables.")
                
            self.instructions.append("CLA")
            self.instructions.append(f"TAD {left.id}")
            
            if isinstance(op, ast.Add):
                self.instructions.append(f"TAD {right.id}")
            elif isinstance(op, ast.BitAnd):
                self.instructions.append(f"AND {right.id}")
            else:
                raise ValueError(f"Unsupported binary operator in PDP-8 compiler: {op}")
                
            self.instructions.append(f"DCA {var_name}")
        else:
            raise ValueError(f"Unsupported value in assignment: {val}")

    def visit_Expr(self, node):
        if isinstance(node.value, ast.Call):
            func = node.value.func
            if isinstance(func, ast.Name) and func.id.upper() == 'HLT':
                self.instructions.append("HLT")
            else:
                self.generic_visit(node)
        else:
            self.generic_visit(node)

    def generate_assembly(self):
        asm = []
        asm.append("# Compiled DEC PDP-8 Assembly Program")
        asm.append("ORG 0")
        
        # In PDP-8 it's good practice to clear Link / Accumulator at startup
        asm.append("CLA CLL")
        
        # Add core instructions (skipping HLT if added manually later)
        inst_list = [inst for inst in self.instructions if inst != "HLT"]
        for inst in inst_list:
            if inst == "CLA" and asm[-1] == "CLA CLL":
                # redundant CLA right after setup, but we'll leave it as is
                pass
            asm.append(inst)
            
        asm.append("HLT")
            
        # Append variables
        asm.append("")
        asm.append("# Variables")
        for var in sorted(self.variables):
            asm.append(f"{var}: DATA 0")
            
        # Append constants
        asm.append("# Constants")
        for val, lbl in sorted(self.constants.items()):
            asm.append(f"{lbl}: DATA {val}")
            
        return "\n".join(asm) + "\n"

class Cray1Codegen(ast.NodeVisitor):
    def __init__(self):
        self.instructions = []

    def visit_Assign(self, node):
        if len(node.targets) != 1:
            raise ValueError("Only single-target assignments are supported.")
        target = node.targets[0]
        val = node.value
        
        # 1. Target is a subscript: mem[A1] = V2
        if isinstance(target, ast.Subscript):
            if not isinstance(target.value, ast.Name) or target.value.id != 'mem':
                raise ValueError("Only 'mem' subscript targets are supported.")
            slice_node = target.slice
            if not isinstance(slice_node, ast.Name):
                raise ValueError("Subscript slice must be a register like A0-A7.")
            
            if not isinstance(val, ast.Name):
                raise ValueError("Right-hand side of memory store must be a vector register like V0-V7.")
                
            self.instructions.append(f"mem[{slice_node.id}] = {val.id}")
            return

        # Target is a name
        if not isinstance(target, ast.Name):
            raise ValueError("Target of assignment must be a register or variable name.")
            
        target_name = target.id
        
        # 2. Assignment to VL: VL = A0
        if target_name.upper() == 'VL':
            if not isinstance(val, ast.Name):
                raise ValueError("Right-hand side of VL assignment must be a register name.")
            self.instructions.append(f"VL = {val.id}")
            return
            
        # 3. Source is Constant: A1 = 4096
        if isinstance(val, ast.Constant):
            self.instructions.append(f"{target_name} = {val.value}")
            return
            
        # 4. Source is Subscript: V1 = mem[A0]
        if isinstance(val, ast.Subscript):
            if not isinstance(val.value, ast.Name) or val.value.id != 'mem':
                raise ValueError("Only 'mem' subscripts are supported.")
            slice_node = val.slice
            if not isinstance(slice_node, ast.Name):
                raise ValueError("Subscript slice must be an address register like A0-A7.")
            self.instructions.append(f"{target_name} = mem[{slice_node.id}]")
            return
            
        # 5. Source is BinOp: V1 = V2 + V3
        if isinstance(val, ast.BinOp):
            left = val.left
            right = val.right
            op = val.op
            
            if not isinstance(left, ast.Name) or not isinstance(right, ast.Name):
                raise ValueError("Vector additions must be between registers.")
                
            if isinstance(op, ast.Add):
                self.instructions.append(f"{target_name} = {left.id} + {right.id}")
            else:
                raise ValueError(f"Unsupported operator in Cray-1 compiler: {op}")
            return

        raise ValueError(f"Unsupported assignment format in Cray-1 compiler.")

    def visit_Expr(self, node):
        if isinstance(node.value, ast.Call):
            func = node.value.func
            if isinstance(func, ast.Name) and func.id.upper() == 'HLT':
                self.instructions.append("HLT")
            else:
                self.generic_visit(node)
        else:
            self.generic_visit(node)

    def generate_assembly(self):
        asm = []
        asm.append("# Compiled Cray-1 Assembly Program")
        for inst in self.instructions:
            asm.append(inst)
            
        # Append HLT if not present
        if not self.instructions or self.instructions[-1] != "HLT":
            asm.append("HLT")
            
        return "\n".join(asm) + "\n"

def main():
    parser = argparse.ArgumentParser(description="Brooks Zoo AST Compiler")
    parser.add_argument("input", help="Path to input Python source code file")
    parser.add_argument("-arch", required=True, choices=["pdp8", "cray1"], help="Target architecture")
    parser.add_argument("-o", "--output", required=True, help="Output assembly path")
    args = parser.parse_args()

    if not os.path.exists(args.input):
        print(f"Error: Input file {args.input} does not exist.")
        sys.exit(1)

    try:
        with open(args.input, "r") as f:
            source = f.read()
            
        tree = ast.parse(source)
        
        if args.arch == "pdp8":
            visitor = Pdp8Codegen()
        else:
            visitor = Cray1Codegen()
            
        visitor.visit(tree)
        asm_output = visitor.generate_assembly()
        
        with open(args.output, "w") as f:
            f.write(asm_output)
            
        print(f"Successfully compiled {args.input} ({args.arch}) to {args.output}")
    except Exception as e:
        print(f"Error compiling source file: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
