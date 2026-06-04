import sys

def assemble(input_text):
    lines = input_text.split('\n')
    output = ["# Compiled ILLIAC IV Hex File"]
    symbols = {}
    current_addr = 0

    # Pass 1: find labels
    for line in lines:
        line = line.strip().split('#')[0].strip()
        if not line:
            continue
        if ':' in line:
            label, rest = line.split(':', 1)
            symbols[label.strip()] = current_addr
            line = rest.strip()
            if not line:
                continue
        parts = line.split(None, 1)
        mnemonic = parts[0].upper()
        if mnemonic in ["HALT", "HLT", "LD_PE", "ST_PE", "ADD_PE", "ROUTE_PE_L", "ROUTE_PE_R", "ADD_PE_REG"]:
            current_addr += 1
        elif mnemonic in ["LD_CU", "ST_CU", "JMP_CU", "JNZ_CU"]:
            current_addr += 2
        elif mnemonic in ["LDI_CU", "STI_CU"]:
            current_addr += 1

    # Pass 2: assemble
    for line in lines:
        line = line.strip().split('#')[0].strip()
        if not line:
            continue
        if ':' in line:
            _, rest = line.split(':', 1)
            line = rest.strip()
            if not line:
                continue
        parts = line.split(None, 1)
        mnemonic = parts[0].upper()

        if mnemonic in ["HALT", "HLT"]:
            output.append("0000")
            continue
        if mnemonic == "LD_PE":
            output.append("7000")
            continue
        if mnemonic == "ST_PE":
            output.append("8000")
            continue
        if mnemonic == "ADD_PE":
            output.append("9000")
            continue
        if mnemonic == "ROUTE_PE_L":
            output.append("A000")
            continue
        if mnemonic == "ROUTE_PE_R":
            output.append("B000")
            continue
        if mnemonic == "ADD_PE_REG":
            output.append("C000")
            continue

        ops = [op.strip() for op in parts[1].split(',')]

        def get_val(op_str):
            if op_str in symbols:
                return symbols[op_str]
            return int(op_str, 0)

        def get_reg(reg_str):
            return int(reg_str.upper().replace('R', ''))

        if mnemonic == "LD_CU":
            rd = get_reg(ops[0])
            addr = get_val(ops[1])
            output.append(f"{0x1000 | (rd << 8):04X}")
            output.append(f"{addr & 0xFFFF:04X}")
        elif mnemonic == "ST_CU":
            rs = get_reg(ops[0])
            addr = get_val(ops[1])
            output.append(f"{0x2000 | (rs << 8):04X}")
            output.append(f"{addr & 0xFFFF:04X}")
        elif mnemonic == "LDI_CU":
            rd = get_reg(ops[0])
            rs = get_reg(ops[1])
            output.append(f"{0x3000 | (rd << 8) | (rs << 4):04X}")
        elif mnemonic == "STI_CU":
            rs = get_reg(ops[0])
            rd = get_reg(ops[1])
            output.append(f"{0x4000 | (rs << 8) | (rd << 4):04X}")
        elif mnemonic == "JNZ_CU":
            rd = get_reg(ops[0])
            addr = get_val(ops[1])
            output.append(f"{0x5000 | (rd << 8):04X}")
            output.append(f"{addr & 0xFFFF:04X}")
        elif mnemonic == "JMP_CU":
            addr = get_val(ops[0])
            output.append("6000")
            output.append(f"{addr & 0xFFFF:04X}")

    return '\n'.join(output) + '\n'

if __name__ == '__main__':
    if len(sys.argv) < 3:
        sys.exit(1)
    with open(sys.argv[1], 'r') as f:
        code = f.read()
    compiled = assemble(code)
    with open(sys.argv[2], 'w') as f:
        f.write(compiled)
