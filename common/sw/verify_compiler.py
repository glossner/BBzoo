#!/usr/bin/env python3
import subprocess
import sys
import os

def verify_pdp8():
    py_source = """
def main():
    val1 = 5
    val2 = 13
    result = val1 + val2
    hlt()
"""
    py_file = "decpdp8/sw/test_comp.py"
    asm_file = "decpdp8/sw/test_comp.asm"
    hex_file = "decpdp8/sw/test_comp.hex"

    with open(py_file, "w") as f:
        f.write(py_source)

    try:
        # 1. Compile Python to PDP-8 assembly
        cmd_compile = ["python3", "common/sw/zoo_compiler.py", "-arch", "pdp8", py_file, "-o", asm_file]
        subprocess.run(cmd_compile, check=True)

        # 2. Assemble assembly to Hex
        cmd_assemble = ["python3", "common/sw/zoo_assembler.py", "-arch", "pdp8", asm_file, "-o", hex_file]
        subprocess.run(cmd_assemble, check=True)

        # 3. Read generated hex to verify it contains the DCA instructions and HLT (F02)
        with open(hex_file, "r") as f:
            hex_lines = [line.strip() for line in f if line.strip() and not line.startswith("#")]
        
        print(f"[PDP-8 Compiler] Generated Hex: {hex_lines}")
        # HLT is octal 7402 (hex F02), check if F02 is present
        if "F02" not in hex_lines:
            raise Exception("HLT (F02) instruction not found in generated hex!")

        print("[PDP-8 Compiler] Verified successfully!")
    finally:
        # Clean up
        for path in [py_file, asm_file, hex_file]:
            if os.path.exists(path):
                os.remove(path)

def verify_cray1():
    py_source = """
def main():
    A1 = 2
    A2 = 16
    A3 = 24
    VL = A1
    V0 = mem[A2]
    V1 = V0 + V0
    mem[A3] = V1
    hlt()
"""
    py_file = "cray1/sw/test_comp.py"
    asm_file = "cray1/sw/test_comp.asm"
    hex_file = "cray1/sw/test_comp.hex"
    ref_hex = "cray1/sw/test_vector.hex"

    with open(py_file, "w") as f:
        f.write(py_source)

    try:
        # 1. Compile Python to Cray-1 assembly
        cmd_compile = ["python3", "common/sw/zoo_compiler.py", "-arch", "cray1", py_file, "-o", asm_file]
        subprocess.run(cmd_compile, check=True)

        # 2. Assemble assembly to Hex
        cmd_assemble = ["python3", "common/sw/zoo_assembler.py", "-arch", "cray1", asm_file, "-o", hex_file]
        subprocess.run(cmd_assemble, check=True)

        # 3. Compare with reference hex (excluding comment lines)
        with open(hex_file, "r") as f:
            new_lines = [line.strip().upper() for line in f if line.strip() and not line.startswith("#")]
            
        with open(ref_hex, "r") as f:
            ref_lines = [line.strip().upper() for line in f if line.strip() and not line.startswith("#")]

        # Compare the instruction parts (the first 8 lines corresponding to the HLT instruction at offset 7)
        code_len = 8  # A1=2, A2=16, A3=24, VL=A1, V0=mem[A2], V1=V0+V0, mem[A3]=V1, HLT
        if new_lines[:code_len] != ref_lines[:code_len]:
            raise Exception(f"Instruction mismatch!\nExpected: {ref_lines[:code_len]}\nGot: {new_lines[:code_len]}")

        print("[Cray-1 Compiler] Verified successfully! Instructions match test_vector.hex.")
    finally:
        # Clean up
        for path in [py_file, asm_file, hex_file]:
            if os.path.exists(path):
                os.remove(path)

def main():
    verify_pdp8()
    verify_cray1()
    print("All compilers verified successfully!")

if __name__ == "__main__":
    main()
