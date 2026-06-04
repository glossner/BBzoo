# Multiflow TRACE Architecture Reference 🏛️

* **Design Year**: 1987
* **Designer**: Josh Fisher / Multiflow team
* **Architecture Paradigm**: VLIW (Very Long Instruction Word)

## Overview
The Multiflow TRACE was the first commercial VLIW computer system, relying heavily on a sophisticated compiler (using trace scheduling) to discover and schedule fine-grained instruction-level parallelism (ILP).

In this model:
- 32-bit data and addressing.
- Unified register file of 8 general purpose registers (R0-R7).
- 64-bit VLIW bundle packing three parallel slots:
  - **ALU Slot**: ADD, SUB, or NOP.
  - **LSU Slot**: LD, ST, LD_CU, or NOP.
  - **Control Slot**: JMP, HLT, or NOP.

## Instruction Set Summary
Each VLIW bundle contains 2 words of 32-bit:
- **ALU Slot**: `ADD/SUB Rd, Rs1, Rs2`
- **LSU Slot**: `LD/ST Reg, Base`
- **Control Slot**: `JMP target` / `HLT`
- **Standalone Directives**: `LD_CU Rd, target` load register immediate pointer.
