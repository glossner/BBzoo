import sys

def assemble(input_text):
    lines = input_text.split('\n')
    output = ["# Compiled IBM MFAST Hex File"]
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
        if mnemonic == "ORG":
            continue
        if mnemonic == "DATA":
            current_addr += 1
        else:
            # VLIW, HLT, JMP, LD_CU all take 32 bits (2 words)
            current_addr += 2

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

        if mnemonic == "DATA":
            val = int(parts[1].strip(), 0)
            output.append(f"{val & 0xFFFF:04X}")
            continue

        if mnemonic == "ORG":
            continue

        if mnemonic in ["HALT", "HLT"]:
            output.append("8000")
            output.append("0000")
            continue

        def get_val(op_str):
            if op_str in symbols:
                return symbols[op_str]
            return int(op_str, 0)

        def get_reg(reg_str):
            return int(reg_str.upper().replace('R', ''))

        if mnemonic == "JMP":
            target = get_val(parts[1].strip())
            output.append("4000")
            output.append(f"{target & 0xFFFF:04X}")
            continue

        if mnemonic == "LD_CU":
            ops = [op.strip() for op in parts[1].split(',')]
            rd = get_reg(ops[0])
            addr = get_val(ops[1])
            output.append(f"{0xC000 | (rd << 8):04X}")
            output.append(f"{addr & 0xFFFF:04X}")
            continue

        # VLIW instruction (ALU ; MAU ; LSU)
        slots = line.split(';')
        if len(slots) != 3:
            raise Exception(f"Invalid MFAST VLIW instruction: {line}")

        alu_field = 0
        mau_field = 0
        lsu_field = 0

        # ALU
        alu_str = slots[0].strip()
        if alu_str.upper() != "NOP":
            aparts = alu_str.split(None, 1)
            aop = aparts[0].upper()
            aops = [op.strip() for op in aparts[1].split(',')]
            ard = get_reg(aops[0])
            ars1 = get_reg(aops[1])
            ars2 = get_reg(aops[2])
            aopcode = {"ADD": 1, "SUB": 2, "AND": 3}[aop]
            alu_field = (aopcode << 8) | (ard << 5) | (ars1 << 2) | (ars2 & 0x3)

        # MAU
        mau_str = slots[1].strip()
        if mau_str.upper() != "NOP":
            mparts = mau_str.split(None, 1)
            mop = mparts[0].upper()
            mops = [op.strip() for op in mparts[1].split(',')]
            mrd = get_reg(mops[0])
            mrs1 = get_reg(mops[1])
            mrs2 = get_reg(mops[2])
            mopcode = {"MUL": 1, "MAC": 2, "MSUB": 3}[mop]
            mau_field = (mopcode << 8) | (mrd << 5) | (mrs1 << 2) | (mrs2 & 0x3)

        # LSU
        lsu_str = slots[2].strip()
        if lsu_str.upper() != "NOP":
            lparts = lsu_str.split(None, 1)
            lop = lparts[0].upper()
            lops = [op.strip() for op in lparts[1].split(',')]
            lreg = get_reg(lops[0])
            lbase = get_reg(lops[1])
            lopcode = {"LD": 1, "ST": 2}[lop]
            lsu_field = (lopcode << 8) | (lreg << 5) | (lbase & 0x1F)

        # Pack
        inst32 = (alu_field << 20) | (mau_field << 10) | lsu_field
        w0 = (inst32 >> 16) & 0xFFFF
        w1 = inst32 & 0xFFFF
        output.append(f"{w0:04X}")
        output.append(f"{w1:04X}")

    return '\n'.join(output) + '\n'

if __name__ == '__main__':
    if len(sys.argv) < 3:
        sys.exit(1)
    with open(sys.argv[1], 'r') as f:
        code = f.read()
    compiled = assemble(code)
    with open(sys.argv[2], 'w') as f:
        f.write(compiled)
