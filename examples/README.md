# BBZoo Assembly Programming & Simulator Guide

Welcome to the **BBZoo** educational workspace! This guide explains how to write custom assembly programs, compile them into machine hex representations, and execute them on the synthesizable hardware simulators.

---

## 1. Architectural Paradigms in the Zoo

BBzoo hosts 64 classic architectures classified into different "Houses" based on their design paradigm:

| House | Paradigm | Examples | Register File Structure |
|---|---|---|---|
| **Accumulator House** | 1-operand accumulator logic | PDP-8, MOS 6502, Intel 8080A | Single Accumulator (A) + temporary registers |
| **RISC House** | 3-operand register-to-register | IBM 801, MIPS I, ARM1, Berkeley RISC-I, SPARC, PowerPC | Large General Purpose Register (GPR) file |
| **Stack House** | 0-operand evaluation stack | Burroughs B5500, HP 3000, UCSD Pascal, JVM | Top-of-Stack (TOS) registers + memory stack |
| **DSP House** | Dedicated MAC and parallel arithmetic | NEC µPD7720, TI TMS32010, TI TMS320C6000 | Separate data/program address spaces, accumulator |
| **GPU House** | Parallel, SIMD, and VLIW execution | NVIDIA GeForce 256, ARM Mali-200, AMD R600 | Vector / VLIW register channels |
| **Bit-Slice House** | Modular bit-slice slice-cascading logic | Am2901, Intel 3002, IMP-16, MC10800 | Cascaded ALUs + general register slices |
| **Array Processor House** | SIMD, Bit-Serial, and VLIW processing elements | ILLIAC IV, ICL DAP, Goodyear MPP, CM-1, MFAST | Central control registers + parallel PE accumulator/RAM arrays |
| **VLIW House** | Explicitly parallel bundle execution | Multiflow TRACE, Cydra 5, TMS320C6000, Crusoe, Itanium | Wide bundle packet routing + static/rotating registers |
| **Vector House** | Parallel vector streaming / registers | Cray-1, STAR-100, TI ASC, Convex C1, NEC SX-2, S/370 VF | GPR scalar base address registers + Vector Register files (V0-V3) |

---

## 2. Writing Assembly Language

Each architecture has its assembler parser defined in `common/sw/arch/`. Here are quick templates for the core paradigms:

### Accumulator Style (e.g., Intel 8080A)
```assembly
LDA valX      # Load memory at valX into accumulator A
MOV B, A      # Move A into register B
ADD B         # A = A + B
STA valY      # Store accumulator A to memory at valY
HLT           # Halt

ORG 32
valX: DATA 15
valY: DATA 0
```

### RISC Style (e.g., MIPS I)
```assembly
LW R1, valF0  # Load memory at valF0 into R1
LW R2, valF1  # Load memory at valF1 into R2
ADDU R3, R2, R1 # R3 = R2 + R1
SW R3, valF2  # Store R3 to memory at valF2
HALT

ORG 64
valF0: DATA 0
valF1: DATA 1
valF2: DATA 0
```

---

## 3. Assembling Programs

Use the master assembler script `zoo_assembler.py` to compile assembly (`.asm`) code into machine-readable hex files (`.hex`):

```bash
# General Command
python3 common/sw/zoo_assembler.py -arch <arch_name> -in <program.asm> -out <program.hex>

# Examples:
python3 common/sw/zoo_assembler.py -arch mips1 -in examples/mips1_fibonacci.asm -out examples/mips1_fibonacci.hex
python3 common/sw/zoo_assembler.py -arch mos6502 -in examples/mos6502_factorial.asm -out examples/mos6502_factorial.hex
python3 common/sw/zoo_assembler.py -arch intel8080a -in examples/intel8080a_loop.asm -out examples/intel8080a_loop.hex
```

---

## 4. Running the Emulator

Use the standalone `SimulatorApp` to load any assembled `.hex` file, execute it cycle-by-cycle on the Chisel hardware core, and display performance counters.

```bash
# General Command
sbt "run --arch <arch_name> --hex <path_to_hex> [--trace] [--cycles <limit>]"

# Examples:
# Run MIPS Fibonacci with cycle-by-cycle logging:
sbt "run --arch mips1 --hex examples/mips1_fibonacci.hex --trace"

# Run MOS 6502 Factorial:
sbt "run --arch mos6502 --hex examples/mos6502_factorial.hex --trace"
```

---

## 5. Interpreting Trace Outputs

When `--trace` is enabled, the emulator prints a cycle-by-cycle register and FSM state report:

```text
[Simulator] Loaded 12 words of program into memory.
[Cycle 1] state=0 pc=0 mem_req=1 addr=0 write=0 wdata=0
[Cycle 2] state=0 pc=1 mem_req=1 addr=1 write=0 wdata=0
[Cycle 3] state=1 pc=2 mem_req=0 addr=0 write=0 wdata=0 [Registers: R1=0 R2=0 R3=0]
...
[Simulator] Simulation halted after 42 cycles.
=== Performance Summary ===
* Total Cycles: 42
* Retired Instructions: 13
* Memory Reads: 21
* Memory Writes: 4
* CPI: 3.23
```
