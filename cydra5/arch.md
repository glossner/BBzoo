# Cydrome Cydra 5 Architecture Reference 🏛️

* **Design Year**: 1987
* **Designer**: Bob Rau / Cydrome team
* **Architecture Paradigm**: VLIW (Very Long Instruction Word) with Rotating Registers

## Overview
The Cydrome Cydra 5 was a pioneering VLIW supercomputer that introduced hardware support for software pipelining of loops. It featured a rotating register file to avoid the overhead of register renaming and code expansion during loop iterations.

In this model:
- 32-bit data and addressing.
- General purpose registers R0-R7, where R4-R7 are **rotating registers** that shift ($R4 \leftarrow R5 \leftarrow R6 \leftarrow R7$) upon execution of a loop branch instruction.
- VLIW execution of parallel slots:
  - **ALU Slot**: ADD, SUB, or NOP.
  - **LSU Slot**: LD, ST, LD_CU, or NOP.
  - **Control Slot**: JMP, BR_ROT, HLT, or NOP.

## Instruction Set Summary
- **ALU Slot**: `ADD/SUB Rd, Rs1, Rs2`
- **LSU Slot**: `LD/ST Reg, Base`
- **Control Slot**: `JMP target` / `BR_ROT target` (decrements loop count and rotates register file R4-R7) / `HLT`
- **Standalone Directives**: `LD_CU Rd, target` load register immediate pointer.
