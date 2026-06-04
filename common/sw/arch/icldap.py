import sys

def assemble(input_text):
    lines = input_text.split('\n')
    output = ["# Compiled ICL DAP Hex File"]
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
        if mnemonic in ["HALT", "HLT", "CLR_CARRY"]:
            current_addr += 1
        elif mnemonic in ["LD_PE_BIT", "ADD_PE_BIT", "ST_PE_BIT"]:
            current_addr += 1
        elif mnemonic in ["LD_CU", "ST_CU"]:
            current_addr += 2
        elif mnemonic == "DATA":
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
        if mnemonic == "CLR_CARRY":
            output.append("6000")
            continue

        if mnemonic == "DATA":
            val = int(parts[1].strip(), 0)
            output.append(f"{val & 0xFFFF:04X}")
            continue
        if mnemonic == "ORG":
            continue

        ops = [op.strip() for op in parts[1].split(',')]

        def get_val(op_str):
            if op_str in symbols:
                return symbols[op_str]
            return int(op_str, 0)

        def get_reg(reg_str):
            return int(reg_str.upper().replace('R', ''))

        if mnemonic == "LD_PE_BIT":
            bit = get_val(ops[0])
            output.append(f"{0x3000 | (bit & 0xF):04X}")
        elif mnemonic == "ADD_PE_BIT":
            bit = get_val(ops[0])
            output.append(f"{0x4000 | (bit & 0xF):04X}")
        elif mnemonic == "ST_PE_BIT":
            bit = get_val(ops[0])
            output.append(f"{0x5000 | (bit & 0xF):04X}")
        elif mnemonic == "LD_CU":
            rd = get_reg(ops[0])
            addr = get_val(ops[1])
            output.append(f"{0x1000 | (rd << 8):04X}")
            output.append(f"{addr & 0xFFFF:04X}")
        elif mnemonic == "ST_CU":
            rs = get_reg(ops[0])
            addr = get_val(ops[1])
            output.append(f"{0x2000 | (rs << 8):04X}")
            output.append(f"{addr & 0xFFFF:04X}")
        else:
            raise Exception(f"Unknown ICL DAP mnemonic: {mnemonic}")

    return '\n'.join(output) + '\n'

if __name__ == '__main__':
    if len(sys.argv) < 3:
        sys.exit(1)
    with open(sys.argv[1], 'r') as f:
        code = f.read()
    compiled = assemble(code)
    with open(sys.argv[2], 'w') as f:
        f.write(compiled)
