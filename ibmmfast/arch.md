# IBM MFAST Architecture Reference 🏛️

* **Design Year**: 1995
* **Designer**: Gerald Pechanek (IBM / MWave team)
* **Architecture Paradigm**: VLIW Parallel DSP Array Processor

## Overview
The IBM MFAST (Mwave Folded Array Signal Transform) was a high-performance parallel DSP architecture designed for communications and signal processing. It features a central Control Unit broadcasting multi-slot VLIW instructions to an array of Processing Elements (PEs) organized in a folded grid. Each PE has a register file, an arithmetic logic unit (ALU), and a multiply-add unit (MAU) executing in parallel.

In this model:
- A central Control Unit (CU) with PC and 4 index registers (R0-R3).
- 4 PEs, each containing a Register File of 8 registers (R0-R7, 16-bit).
- 32-bit VLIW instruction format packing Control operations, an ALU slot (ADD, SUB, AND), a MAU slot (MUL, MAC, MSUB), and an LSU slot (LD, ST) in parallel.

## Instruction Set Summary
Instructions are 32-bit wide (two 16-bit words) and data values are 16-bit wide.

### Control and CU Instructions
- `HLT` (`0x8000_0000`): Halt execution.
- `JMP target` (`0x4000_0000 | target`): Jump to 16-bit target address.
- `LD_CU rd, addr` (`0xC000_0000 | (rd << 24) | addr`): Load immediate address pointer into CU register `rd`.

### VLIW Slots
VLIW instructions pack ALU, MAU, and LSU slots together:
- **ALU Slot**: `ADD/SUB/AND Rd, Rs1, Rs2`
- **MAU Slot**: `MUL/MAC/MSUB Rd, Rs1, Rs2`
- **LSU Slot**: `LD/ST PE_Reg, CU_Reg` (load/store PE register from memory address `CU_Reg + PE_index`).
