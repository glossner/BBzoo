#!/usr/bin/env python3
import subprocess
import sys
import os

def main():
    asm_source = """
    move.l #4096, %a0
    move.l #15, %d0
    move.l #27, %d1
    add.l %d1, %d0
    move.l %d0, (%a0)
    loop:
    bra loop
    """
    
    obj_file = "motorola68000/sw/test_elf_verify.o"
    hex_file = "motorola68000/sw/test_elf_verify.hex"
    ref_file = "motorola68000/sw/test_add.hex"
    
    # 1. Compile assembly to ELF object
    print("[M68k ELF] Compiling assembly with clang...")
    cmd_compile = [
        "clang", "-target", "m68k-elf", "-c", "-x", "assembler", "-", "-o", obj_file
    ]
    res_compile = subprocess.run(cmd_compile, input=asm_source, capture_output=True, text=True)
    if res_compile.returncode != 0:
        print("Compilation failed:")
        print(res_compile.stderr)
        sys.exit(1)
        
    # 2. Convert ELF to Hex
    print("[M68k ELF] Converting ELF to Hex...")
    cmd_load = [
        "python3", "common/sw/elf_loader.py", "-arch", "m68k", obj_file, "-o", hex_file
    ]
    res_load = subprocess.run(cmd_load, capture_output=True, text=True)
    if res_load.returncode != 0:
        print("ELF Loader conversion failed:")
        print(res_load.stderr)
        sys.exit(1)
        
    # 3. Read and compare hex outputs
    with open(ref_file, 'r') as f:
        ref_data = [line.strip().upper() for line in f if line.strip() and not line.strip().startswith('#')]
        
    with open(hex_file, 'r') as f:
        new_data = [line.strip().upper() for line in f if line.strip() and not line.strip().startswith('#')]
        
    # Clean up generated files
    for path in [obj_file, hex_file]:
        if os.path.exists(path):
            os.remove(path)
            
    if ref_data != new_data:
        print("[M68k ELF] Mismatch!")
        print("Reference:", ref_data)
        print("Generated:", new_data)
        sys.exit(1)
        
    print("[M68k ELF] Compiled and verified successfully via clang cross-compilation pipeline!")
    sys.exit(0)

if __name__ == '__main__':
    main()
