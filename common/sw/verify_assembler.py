import subprocess
import sys
import os

def verify_hex(arch, asm_file, orig_hex_file):
    assembled_hex = orig_hex_file + ".new"
    
    cmd = ["python3", "common/sw/zoo_assembler.py", "-arch", arch, asm_file, "-o", assembled_hex]
    res = subprocess.run(cmd, capture_output=True, text=True)
    if res.returncode != 0:
        print(f"[{arch}] Assembly failed:")
        print(res.stderr)
        return False

    # Read original hex lines (excluding comments)
    with open(orig_hex_file, 'r') as f:
        orig_data = [line.strip().upper() for line in f if line.strip() and not line.strip().startswith('#')]

    # Read assembled hex lines (excluding comments)
    with open(assembled_hex, 'r') as f:
        new_data = [line.strip().upper() for line in f if line.strip() and not line.strip().startswith('#')]

    # Clean up generated file
    if os.path.exists(assembled_hex):
        os.remove(assembled_hex)

    if orig_data != new_data:
        print(f"[{arch}] Hex mismatch!")
        print("Original:", orig_data)
        print("Assembled:", new_data)
        return False

    print(f"[{arch}] Verified successfully!")
    return True

def main():
    success = True
    success &= verify_hex("pdp8", "decpdp8/sw/test_add.asm", "decpdp8/sw/test_add.hex")
    success &= verify_hex("ibm360", "ibm360/sw/test_add.asm", "ibm360/sw/test_add.hex")
    success &= verify_hex("cray1", "cray1/sw/test_vector.asm", "cray1/sw/test_vector.hex")
    success &= verify_hex("m68k", "motorola68000/sw/test_add.asm", "motorola68000/sw/test_add.hex")

    if not success:
        sys.exit(1)
    print("All assemblers match original test hex files!")

if __name__ == '__main__':
    main()
