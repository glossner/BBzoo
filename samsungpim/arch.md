# Samsung HBM2-PIM (Aquabolt-XL) 🏛️

## Introduction
* **Designer/Team:** Samsung Electronics
* **Year:** 2021
* **Paradigm:** High-Bandwidth Memory Processing in Memory (HBM-PIM)

## Unique Architectural Features
- **In-Bank Processing:** Programmable Computing Units (PCUs) embedded directly inside DRAM bank structures.
- **AI Acceleration:** Targets machine learning and arithmetic kernels (GEMM, LSTM) directly inside the storage stack.
- **Double Performance, Half Energy:** Avoids data transit across the external DDR/HBM bus, maximizing efficiency.

## Instruction Formats & Opcodes
- `PCU_LD Vd, addr` — Vector Load: Opcode `0x01`. Loads 4 words into PCU vector Vd.
- `PCU_ST Vs, addr` — Vector Store: Opcode `0x02`. Stores 4 words from PCU vector Vs.
- `PCU_ADD Vd, Va, Vb` — Vector Add: Opcode `0x03`. Adds vectors Va and Vb.
- `HLT` — Halt: Opcode `0x3F`.

## Assembly Stress Program (Vector Addition)
```assembly
# Samsung HBM2-PIM Vector Addition
PCU_LD V1, valA0
PCU_LD V2, valB0
PCU_ADD V3, V1, V2
PCU_ST V3, valC0
HALT
```
