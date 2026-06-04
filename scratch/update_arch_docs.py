import os

docs = {
    # 1. Pioneer House
    "babbage": {
        "purpose": "Automating mathematical table calculations (like polynomials and logarithms) mechanically to eliminate human computation errors.",
        "program": """# Babbage Polynomial Evaluation: y = (x + a) - b
L 20    # Load x from Store column 20
ADD 21  # Add constant a from Store column 21
SUB 22  # Subtract constant b from Store column 22
S 23    # Store y to Store column 23
HLT"""
    },
    "harvardmark1": {
        "purpose": "Performing long, complex scientific and ballistics calculations automatically using electro-mechanical relay sequences.",
        "program": """# Harvard Mark I Polynomial Evaluation: y = (x + a) - b
# Registers: R20 (x), R21 (a), R22 (b), R23 (y)
MOV R20, R23
ADD R21, R23
SUB R22, R23
HLT"""
    },
    "zusez1": {
        "purpose": "Solving complex engineering calculations and structural formulas automatically using binary floating-point mechanical gates.",
        "program": """# Zuse Z1 Polynomial Evaluation: y = (x + a) - b
PR 20   # Load x into register R1
MOV R1, R2 # Move x to R2
PR 21   # Load a into R1
ADD     # R1 = x + a
MOV R1, R2 # Move sum to R2
PR 22   # Load b into R1
SUB     # R1 = (x + a) - b
PS 23   # Store result y to address 23
HLT"""
    },
    "manchestermu1": {
        "purpose": "Validating the feasibility of electronic stored-program computing and CRT electrostatic random-access memory (Williams Tube).",
        "program": """# Manchester Baby Polynomial Evaluation: y = (x + a) - b
LDN 20  # Load -x from address 20
SUB 21  # Subtract a from address 21 (ACC = -x - a)
STO 23  # Store temp = -(x + a) to address 23
LDN 22  # Load -b from address 22
SUB 23  # Subtract temp (ACC = -b - (-(x+a)) = x + a - b)
STO 24  # Store y to address 24
STP"""
    },
    "univac1": {
        "purpose": "Large-scale commercial data processing, business accounting, and census tabulation.",
        "program": """# Univac I Polynomial Evaluation: y = (x + a) - b
B 20    # Load x into accumulator
A 21    # Add a to accumulator
S 22    # Subtract b from accumulator
H 23    # Store y
Q       # Quit"""
    },

    # 2. Von Neumann House
    "princetonias": {
        "purpose": "Solving scientific, meteorological, and defense-related mathematical models using a parallel binary accumulator architecture.",
        "program": """# Princeton IAS Polynomial Evaluation: y = (x + a) - b
LD 20
ADD 21
SUB 22
ST 23
HLT"""
    },
    "cambridgeedsac": {
        "purpose": "Performing general-purpose scientific computations using a subroutine-based mercury delay-line memory accumulator.",
        "program": """# Cambridge EDSAC Polynomial Evaluation: y = (x + a) - b
T 30    # Clear accumulator
A 20    # Add x into accumulator (Load x)
A 21    # Add a (ACC = x + a)
S 22    # Subtract b (ACC = x + a - b)
T 23    # Store y and clear accumulator
Z       # Halt"""
    },
    "ibm701": {
        "purpose": "Large-scale scientific modeling, defense calculations, and military cryptanalysis.",
        "program": """# IBM 701 Polynomial Evaluation: y = (x + a) - b
LD 20
ADD 21
SUB 22
ST 23
HLT"""
    },
    "ibm704": {
        "purpose": "High-performance scientific calculations introducing hardware floating-point support and index registers.",
        "program": """# IBM 704 Polynomial Evaluation: y = (x + a) - b
LD 20
ADD 21
SUB 22
ST 23
HLT"""
    },

    # 3. IBM House
    "ibm650": {
        "purpose": "Medium-scale business accounting, payroll, and educational/scientific data processing using magnetic drum memory.",
        "program": """# IBM 650 Polynomial Evaluation: y = (x + a) - b
# Instructions chain next-instruction addresses on the magnetic drum
LD valX, inst1
inst1: ADD valA, inst2
inst2: ST valY, inst3
inst3: HLT
valX: DATA 10
valA: DATA 20
valY: DATA 0"""
    },
    "ibm705": {
        "purpose": "Large-scale commercial record processing, business accounting, and inventory tracking utilizing variable character-field lengths.",
        "program": """# IBM 705 Polynomial Evaluation: y = (x + a) - b
LD 20
ADD 21
SUB 22
ST 23
HLT"""
    },
    "ibm1401": {
        "purpose": "Pervasive commercial business data processing, card manipulation, and high-speed report printing.",
        "program": """# IBM 1401 Polynomial Evaluation: y = x + a (memory-to-memory copy and arithmetic)
MC 20, 22  # Move character x to y
A 21, 22   # Add character a to y
HLT"""
    },

    # 4. Explorer House
    "stczebra": {
        "purpose": "Exploring microprogrammed CPU designs to simplify hardware using functional-bit binary instruction layouts.",
        "program": """# STC ZEBRA Polynomial evaluation: y = x + a
LD 20
ADD 21
ST 22
HLT"""
    },
    "bullgamma60": {
        "purpose": "Exploring hardware concurrency and early multitasking execution across multiple independent processing units.",
        "program": """# Bull Gamma 60 Parallel Evaluation: acc0 = x + a
LD acc0, 20
ADD acc0, 21
ST acc0, 22
HLT"""
    },
    "ibmstretch": {
        "purpose": "Pioneering high-speed pipelined supercomputing with multi-register indexing, branch lookahead, and decimal math.",
        "program": """# IBM Stretch Indexed Polynomial evaluation: y = x + a
LDX X0, 0
LD ACC, 20(X0)
ADD ACC, 21(X0)
ST ACC, 22(X0)
HLT"""
    },

    # 5. Stack House
    "burroughsb5500": {
        "purpose": "Direct high-level language (Algol) compiler target execution utilizing evaluation stacks in hardware.",
        "program": """# Burroughs B5500 Stack expression evaluation: y = (x + a) - b
PUSH 20   # Push x
PUSH 21   # Push a
ADD       # Add (x + a)
PUSH 22   # Push b
SUB       # Subtract ((x + a) - b)
POP 23    # Pop and store to y
HLT"""
    },
    "hp3000": {
        "purpose": "Multiprogramming and general-purpose system computing utilizing a compiler-friendly stack-based OS execution model.",
        "program": """# HP 3000 Stack expression evaluation: y = x + a
PUSH 20   # Push x
PUSH 21   # Push a
ADD       # Add (x + a)
POP 22    # Pop and store to y
HALT"""
    },
    "ethlilith": {
        "purpose": "Direct, high-performance execution of Modula-2 programs on a stack-oriented academic workstation.",
        "program": """# Ethlilith Stack expression evaluation: y = x + a
PUSH 20   # Push x
PUSH 21   # Push a
ADD       # Add (x + a)
POP 22    # Pop and store to y
HALT"""
    },
    "ucsdp": {
        "purpose": "Portable Pascal P-code execution in hardware to achieve cross-platform software distribution.",
        "program": """# UCSD Pascal P-Machine Stack evaluation: y = x + a
PUSH 20   # Push x
PUSH 21   # Push a
ADD       # Add (x + a)
POP 22    # Pop and store to y
HALT"""
    },

    # 6. Bell House
    "decpdp8": {
        "purpose": "Low-cost minicomputer designed for laboratory automation, process control, and industrial interfacing.",
        "program": """# DEC PDP-8 Loop Process: y = x + a
CLA CLL
TAD 20    # Load x
TAD 21    # Add a
DCA 22    # Store to y
HLT"""
    },
    "decpdp11": {
        "purpose": "Highly versatile general-purpose minicomputer and time-sharing system introducing a clean, orthogonal addressing model.",
        "program": """# DEC PDP-11 Polynomial evaluation: y = (x + a) - b
MOV #20, R0    # Address of x
MOV #21, R1    # Address of a
MOV #22, R2    # Address of b
MOV (R0), R3   # Load x
ADD (R1), R3   # Add a
SUB (R2), R3   # Subtract b
MOV #23, R4    # Address of y
MOV R3, (R4)   # Store y
HALT"""
    },
    "decvax": {
        "purpose": "Virtual memory VAX minicomputer for enterprise computing featuring a comprehensive orthogonal instruction set.",
        "program": """# DEC VAX Polynomial evaluation: y = (x + a) - b
MOVL #20, R0
MOVL #21, R1
MOVL #22, R2
MOVL (R0), R3
ADDL2 (R1), R3
SUBL2 (R2), R3
MOVL #23, R4
MOVL R3, (R4)
HALT"""
    },

    # 7. General Register House
    "ibm360": {
        "purpose": "A unified, single ISA mainframe family consolidating business and scientific calculations under one architecture.",
        "program": """# IBM System/360 Polynomial evaluation: y = (x + a) - b
L 1, 20    # Load x into register 1
A 1, 21    # Add a
S 1, 22    # Subtract b
ST 1, 23   # Store y"""
    },
    "motorola68000": {
        "purpose": "Powerful microprocessor target for UNIX workstations, arcade boards, and consumer computing featuring a flat 32-bit register file.",
        "program": """# Motorola 68000 Polynomial evaluation: y = (x + a) - b
MOVEA.L #20, A0
MOVE.L (A0), D0
MOVEA.L #21, A1
ADD.L (A1), D0
MOVEA.L #22, A2
SUB.L (A2), D0
MOVEA.L #23, A3
MOVE.L D0, (A3)"""
    },

    # 8. Cray House
    "univac1103a": {
        "purpose": "Aerospace simulation, cryptanalysis, and military scientific calculation using memory-to-memory two-address instructions.",
        "program": """# Univac 1103A Polynomial evaluation: y = (x + a) - b
TP 20, 23    # Transmit Positive (copy x to y)
ADD 21, 23   # Add a to y
SUB 22, 23   # Subtract b from y
HLT"""
    },
    "cdc6600ppu": {
        "purpose": "Offloading input/output (I/O) processing and network control from the main central processor of the supercomputer.",
        "program": """# CDC 6600 PPU I/O Processing: y = x + a
LD 20
ADD 21
ST 22"""
    },
    "cdc6600": {
        "purpose": "Pioneering scientific supercomputing with parallel functional units and address-register-triggered load/store logic.",
        "program": """# CDC 6600 Parallel evaluation: y = x + a
A1 = B0 + 20   # Load x into X1
A2 = B0 + 21   # Load a into X2
X6 = X1 + X2   # X6 = x + a
A6 = B0 + 22   # Store X6 to address 22"""
    },
    "cray1": {
        "purpose": "Vector pipeline scientific supercomputing for fluid dynamics, physics simulation, and meteorology.",
        "program": """# Cray-1 Vector Addition: Vector C = Vector A + Vector B
A1 = 4         # Set vector length to 4
A2 = 16        # Base address of Vector A
A3 = 20        # Base address of Vector B
A4 = 24        # Base address of Vector C
VL = A1
V0 = mem[A2]   # Load Vector A
V1 = mem[A3]   # Load Vector B
V2 = V0 + V1   # Vector Add
mem[A4] = V2   # Store Vector C
HLT"""
    },

    # 9. Microcomputer House
    "mos6502": {
        "purpose": "Cost-sensitive consumer home microcomputing and video game console processing using zero-page registers.",
        "program": """# MOS 6502 Arithmetic: y = x + a
LDA 20
CLC
ADC 21
STA 22"""
    },
    "intel8080a": {
        "purpose": "Early general-purpose personal computing and industrial embedded controllers running CP/M.",
        "program": """# Intel 8080A Arithmetic: y = x + a
LDA 20
MOV B, A
LDA 21
ADD B
STA 22"""
    },
    "motorola6800": {
        "purpose": "Early industrial process instrumentation and low-cost microcomputer system control.",
        "program": """# Motorola 6800 Arithmetic: y = x + a
LDAA 20
ADDA 21
STAA 22"""
    },

    # 10. RISC House
    "ibm6150": {
        "purpose": "High-performance computer-aided design (CAD) workstations utilizing early pipelined RISC execution paradigms.",
        "program": """# IBM 6150 RT PC arithmetic: y = x + a
L R0, 20
L R1, 21
A R0, R1
ST R0, 22"""
    },
    "mips1": {
        "purpose": "High-performance workstation and enterprise database query acceleration using a clean, pipelined load-store RISC ISA.",
        "program": """# MIPS I arithmetic: y = x + a
LW R1, 20
LW R2, 21
ADDU R1, R1, R2
SW R1, 22
HALT"""
    },
    "arm1": {
        "purpose": "Low-power mobile computing and coprocessor acceleration introducing conditional instructions and barrel shift logic.",
        "program": """# ARM1 arithmetic: y = x + a
LDR R1, 20
LDR R2, 21
ADD R1, R1, R2
STR R1, 22"""
    },
    "berkeleyrisc": {
        "purpose": "Exploring register window designs and simplified load-store RISC instruction layouts to optimize compiler compilation.",
        "program": """# Berkeley RISC-I arithmetic: y = x + a
LD R1, 20
LD R2, 21
ADD R1, R1, R2
ST R1, 22"""
    },

    # 11. DSP House
    "upd7720": {
        "purpose": "Voice-band telecom signal filter operations and real-time modem processing.",
        "program": """# NEC uPD7720 DSP arithmetic: y = x + a
LD A, 20
LD B, 21
ADD
ST A, 22"""
    },
    "tms32010": {
        "purpose": "High-speed voice, speech synthesis, and radar digital signal processing utilizing a hardware multiplier.",
        "program": """# TI TMS32010 DSP arithmetic: y = x + a
LAC 20
ADD 21
SACL 22"""
    },
    "adsp2100": {
        "purpose": "Real-time acoustic analysis, audio filter processing, and industrial control instrumentation.",
        "program": """# ADI ADSP-2100 DSP arithmetic: y = x + a
LD AX0, 20
LD AY0, 21
ADD AR
ST AR, 22"""
    },
    "ibmmwave": {
        "purpose": "High-speed desktop multimedia audio, telephony, and dial-up modem signal processing.",
        "program": """# IBM MWave DSP arithmetic: y = x + a
LD R1, 20
LD R2, 21
ADD
ST R1, 22"""
    },

    # 12. GPU House
    "voodoo1": {
        "purpose": "High-speed 3D hardware rasterization, texture mapping, and depth sorting for gaming PCs.",
        "program": """# 3dfx Voodoo1 texture blend: colorC = colorA + colorB
LD R0, 20
LD R1, 21
ADD R0, R0, R1
ST R0, 22"""
    },
    "geforce256": {
        "purpose": "Introducing hardware Transform & Lighting (T&L) and configurable Register Combiners for multi-texture pixel blending.",
        "program": """# NVIDIA GeForce 256 texture blend: R0 = (R0 * R1) + (R2 * R3)
LD R1, 24    # Scale factor 1
LD R3, 24    # Scale factor 1
LD R0, 20    # Input color A
LD R2, 21    # Input color B
COMBINE R0, R0, R1, R2, R3
ST R0, 22"""
    },
    "radeonr100": {
        "purpose": "Environmental texture mapping and programmable pixel tapestries for early 3D shader simulation.",
        "program": """# ATI Radeon R100 Pixel Tapestry: R0 = (R0 * R1) + R2
LD R1, 24    # Scale factor 1
LD R0, 20    # Input color A
LD R2, 21    # Input color B
TAPESTRY R0, R0, R1, R2
ST R0, 22"""
    },
    "powervr1": {
        "purpose": "Tile-Based Deferred Rendering with hardware Hidden Surface Removal (HSR) depth sorting.",
        "program": """# PowerVR Series 1 Fragment HSR: colorC = HSR(colorA, colorB, Depth)
LD R3, 24    # Fragment Depth
LD R0, 20    # Color A
LD R1, 21    # Color B
HSR R0, R0, R1, R3
ST R0, 22"""
    },
    "mali200": {
        "purpose": "Embedded and mobile 3D vertex processing and texture rendering utilizing a compact vector design.",
        "program": """# ARM Mali-200 Vector blend: V2 = Vector A + Vector B
VLD V0, 20
VLD V1, 21
VADD V2, V0, V1
VST V2, 22
HALT"""
    },
    "amdr600": {
        "purpose": "Unified shader processing running VLIW instructions for parallel vertex, pixel, and physics computation.",
        "program": """# AMD R600 VLIW Arithmetic: R0 = R0 + R1
NOP | LD R0, 20
NOP | LD R1, 21
ADD R0, R0, R1 | NOP
NOP | ST R0, 22"""
    }
}

# Mapping alternate directory names if any (e.g. decpdp8 -> pdp8 or decpdp8, etc.)
# BrooksZoo matches:
# babbage, harvardmark1, zusez1, manchestermu1, univac1, princetonias, cambridgeedsac,
# ibm701, ibm704, ibm650, ibm705, ibm1401, stczebra, bullgamma60, ibmstretch,
# burroughsb5500, decpdp8, decpdp11, ibm360, motorola68000, cray1, univac1103a,
# cdc6600ppu, cdc6600, decvax, intel8080a, motorola6800, ibm6150, mips1, arm1,
# berkeleyrisc, hp3000, ethlilith, ucsdp, upd7720, tms32010, adsp2100, ibmmwave,
# voodoo1, geforce256, radeonr100, powervr1, mali200, amdr600, mos6502.
# Wait, let's verify if folder names match exactly.
# From find command:
# ./adsp2100, ./amdr600, ./arm1, ./babbage, ./berkeleyrisc, ./bullgamma60, ./burroughsb5500,
# ./cambridgeedsac, ./cdc6600ppu, ./cdc6600, ./cray1, ./decpdp11, ./decpdp8, ./decvax,
# ./ethlilith, ./geforce256, ./harvardmark1, ./hp3000, ./ibm1401, ./ibm360, ./ibm6150,
# ./ibm650, ./ibm701, ./ibm704, ./ibm705, ./ibmmwave, ./ibmstretch, ./intel8080a, ./mali200,
# ./manchestermu1, ./mips1, ./mos6502, ./motorola6800, ./motorola68000, ./powervr1,
# ./princetonias, ./radeonr100, ./stczebra, ./tms32010, ./ucsdp, ./univac1, ./univac1103a,
# ./upd7720, ./voodoo1, ./zusez1.
# All folders match the dict keys except decpdp8, decpdp11, motorola68000. Let's map directory names:

dir_map = {
    "decpdp8": "decpdp8",
    "decpdp11": "decpdp11",
    "motorola68000": "motorola68000"
}

for key, val in docs.items():
    folder_name = dir_map.get(key, key)
    path = os.path.join(folder_name, "arch.md")
    if not os.path.exists(path):
        print(f"Warning: {path} does not exist!")
        continue
        
    with open(path, "r") as f:
        content = f.read()
        
    # Strip any existing Architectural Design Purpose / Stress Program sections
    if "## Architectural Design Purpose" in content:
        content = content.split("## Architectural Design Purpose")[0].rstrip()
        
    # Add new sections
    section = f"""

## Architectural Design Purpose

{val['purpose']}

## Target Purpose Stress Program

```assembly
{val['program']}
```
"""
    new_content = content.rstrip() + section
    with open(path, "w") as f:
        f.write(new_content)
    print(f"Updated {path}")
