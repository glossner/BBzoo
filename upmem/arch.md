# UPMEM DPU (DRAM Processing Unit) 🏛️

## Introduction
* **Designer/Team:** UPMEM
* **Year:** 2019
* **Paradigm:** General-Purpose Processing in Memory (PIM)

## Unique Architectural Features
- **DRAM Integrated Cores:** Places small, power-efficient RISC-like cores (DPUs) directly inside DDR4 DRAM chips.
- **WRAM & MRAM Hierarchy:** Uses fast Working RAM (WRAM) local cache/SRAM and high-capacity Main DRAM (MRAM).
- **Scalable Parallelism:** Allows hundreds of parallel DPUs to run independently on local memory banks.

## Instruction Formats & Opcodes
- `ADD Rd, Rs1, Rs2` — Add: Opcode `0x01`. `Rd = Rs1 + Rs2`.
- `LW Rd, Rs, offset` — Load Word: Opcode `0x02`. Loads from `Rs + offset` into Rd.
- `SW Rd, Rs, offset` — Store Word: Opcode `0x03`. Stores from Rd to `Rs + offset`.
- `SUB Rd, Rs1, Rs2` — Subtract: Opcode `0x04`. `Rd = Rs1 - Rs2`.
- `ADDI Rd, Rs1, imm` — Add Immediate: Opcode `0x05`. `Rd = Rs1 + imm`.
- `BNE Rs1, Rs2, offset` — Branch if Not Equal: Opcode `0x06`. PC-relative branch.
- `HLT` — Halt: Opcode `0x3F`.

## Assembly Stress Program (Vector Addition)
```assembly
# UPMEM DPU Vector Addition
LW R1, R0, valA0
LW R2, R0, valB0
ADD R3, R1, R2
SW R3, R0, valC0

LW R1, R0, valA1
LW R2, R0, valB1
ADD R3, R1, R2
SW R3, R0, valC1

LW R1, R0, valA2
LW R2, R0, valB2
ADD R3, R1, R2
SW R3, R0, valC2

LW R1, R0, valA3
LW R2, R0, valB3
ADD R3, R1, R2
SW R3, R0, valC3
HALT
```
