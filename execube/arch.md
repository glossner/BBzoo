# IBM Execube (Multiprocessor PIM) 🏛️

## Introduction
* **Designer/Team:** IBM / Notre Dame (Peter Kogge et al.)
* **Year:** 1993
* **Paradigm:** Multiprocessor Processing in Memory (PIM)

## Unique Architectural Features
- **Embedded Multiprocessing:** Integrates multiple simple Processing Elements (PEs) directly with memory blocks on a single die.
- **PE Local Regs/Accums:** Each PE executes local arithmetic concurrently on its portion of the memory hierarchy.
- **Early PIM Pioneer:** One of the earliest chips implementing logic-enhanced memory designs.

## Instruction Formats & Opcodes
- `PE_LOAD R_pe, addr` — PE Load: Opcode `0x01`. Loads memory at `addr + R_pe` into PE register B, pushing the previous value to register A.
- `PE_ADD R_pe, Rs1, Rs2` — PE Add: Opcode `0x02`. Computes `register B = register A + register B` locally in PE `R_pe`.
- `PE_STORE R_pe, addr` — PE Store: Opcode `0x03`. Stores PE register B value to memory at `addr + R_pe`.
- `HLT` — Halt: Opcode `0x3F`.

## Assembly Stress Program (Vector Addition)
```assembly
# IBM Execube Vector Addition
PE_LOAD R0, valA0
PE_LOAD R0, valB0
PE_ADD R0, R0, R0
PE_LOAD R1, valA0
PE_LOAD R1, valB0
PE_ADD R1, R1, R1
PE_LOAD R2, valA0
PE_LOAD R2, valB0
PE_ADD R2, R2, R2
PE_LOAD R3, valA0
PE_LOAD R3, valB0
PE_ADD R3, R3, R3
PE_STORE R0, valC0
PE_STORE R1, valC0
PE_STORE R2, valC0
PE_STORE R3, valC0
HALT
```
