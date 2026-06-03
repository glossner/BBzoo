#!/usr/bin/env python3
import sys
import os
import argparse
from elftools.elf.elffile import ELFFile

def parse_elf(filename, base_address=0):
    """
    Parses an ELF file (linked binary or relocatable object file)
    and returns a dict mapping physical memory addresses to byte values.
    """
    with open(filename, 'rb') as f:
        elffile = ELFFile(f)
        
        # Check if the ELF has segments (linked binary)
        has_segments = elffile.num_segments() > 0
        program_data = {}
        
        if has_segments:
            # Parse segments (PT_LOAD) for linked binaries
            for segment in elffile.iter_segments():
                if segment['p_type'] == 'PT_LOAD':
                    addr = segment['p_paddr']
                    data = segment.data()
                    for offset, byte in enumerate(data):
                        program_data[addr + offset] = byte
        else:
            # Parse sections (.text, .rodata, .data) for relocatable object files (.o)
            current_addr = base_address
            for section_name in ['.text', '.rodata', '.data']:
                section = elffile.get_section_by_name(section_name)
                if section:
                    data = section.data()
                    for offset, byte in enumerate(data):
                        program_data[current_addr + offset] = byte
                    # Align to next 4-byte boundary
                    current_addr += len(data)
                    current_addr = (current_addr + 3) & ~3
                    
        return program_data

def format_hex(program_data, arch):
    """
    Formats the parsed bytes into the target architecture's hex representation.
    """
    if not program_data:
        return ""

    min_addr = 0
    max_addr = max(program_data.keys())

    lines = []
    if arch == 'm68k':
        # Motorola 68000: 16-bit big-endian words
        lines.append("# Compiled Motorola 68000 ELF Hex File")
        # Ensure max_addr aligns to even boundary
        end_addr = max_addr if max_addr % 2 != 0 else max_addr + 1
        for addr in range(min_addr, end_addr + 1, 2):
            b0 = program_data.get(addr, 0)
            b1 = program_data.get(addr + 1, 0)
            word = (b0 << 8) | b1
            lines.append(f"{word & 0xFFFF:04X}")
            
    elif arch == 'ibm360':
        # IBM System/360: 8-bit bytes
        lines.append("# Compiled IBM System/360 ELF Hex File")
        for addr in range(min_addr, max_addr + 1):
            byte = program_data.get(addr, 0)
            lines.append(f"{byte & 0xFF:02X}")
            
    elif arch == 'cray1':
        # Cray-1: 64-bit big-endian words
        lines.append("# Compiled Cray-1 ELF Hex File")
        # Align to 8-byte boundary
        rem = (max_addr + 1) % 8
        end_addr = max_addr if rem == 0 else max_addr + (8 - rem)
        for addr in range(min_addr, end_addr + 1, 8):
            val = 0
            for i in range(8):
                byte = program_data.get(addr + i, 0)
                val = (val << 8) | byte
            lines.append(f"{val:016X}")
            
    else:
        raise ValueError(f"Unsupported architecture for ELF loader: {arch}")
        
    return "\n".join(lines) + "\n"

def main():
    parser = argparse.ArgumentParser(description="Brooks Zoo ELF to Hex Loader")
    parser.add_argument("input", help="Path to ELF binary or object file (.o)")
    parser.add_argument("-arch", required=True, choices=["m68k", "ibm360", "cray1"], help="Target architecture")
    parser.add_argument("-o", "--output", required=True, help="Output hex file path")
    parser.add_argument("--base", type=int, default=0, help="Base address for relocatable object loading")
    
    args = parser.parse_args()
    
    if not os.path.exists(args.input):
        print(f"Error: Input file {args.input} does not exist.")
        sys.exit(1)
        
    try:
        data = parse_elf(args.input, args.base)
        hex_output = format_hex(data, args.arch)
        with open(args.output, "w") as f:
            f.write(hex_output)
        print(f"Successfully converted {args.input} ({args.arch}) to {args.output}")
    except Exception as e:
        print(f"Error converting ELF file: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
