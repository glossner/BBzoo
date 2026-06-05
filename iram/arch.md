# UC Berkeley IRAM (Intelligent RAM) 🏛️

## Introduction
* **Designer/Team:** UC Berkeley (David Patterson et al.)
* **Year:** 1997
* **Paradigm:** Vector Processor in Memory (PIM)

## Unique Architectural Features
- **Merged DRAM/Logic Process:** Integrates vector processing logic directly on the DRAM silicon die to exploit wide on-chip memory interfaces.
- **Vector Registers:** Includes a set of 8 vector registers (V0-V7), each holding 4 elements of 32-bit width.
- **Latency & Bandwidth Optimization:** Bypasses the traditional system bus (Memory Wall) by routing high-bandwidth internal busses directly between logic and embedded memory banks.

## Instruction Formats & Opcodes
- `VLD Vd, Rs` — Vector Load: Opcode `0x01`. Loads 4 consecutive 32-bit memory words from the address in Rs into Vd.
- `VST Vs, Rd` — Vector Store: Opcode `0x02`. Stores 4 consecutive 32-bit vector elements from Vs to memory starting at address in Rd.
- `VADD.W Vd, Va, Vb` — Vector Integer Add: Opcode `0x03`. Adds elements of vector registers Va and Vb and stores the results in Vd.
- `LW Rd, addr` — Scalar Load: Opcode `0x04`. Loads 32-bit scalar word.
- `SW Rs, addr` — Scalar Store: Opcode `0x05`. Stores 32-bit scalar word.
- `HLT` — Halt: Opcode `0x3F`.

## Assembly Stress Program (Vector Addition)
```assembly
# IRAM Vector Addition
LW R10, valA0
LW R11, valB0
LW R12, valC0
VLD V1, R10
VLD V2, R11
VADD.W V3, V1, V2
VST V3, R12
HALT
```
