# TI TMS320C6000 Architecture Reference 🏛️

* **Design Year**: 1997
* **Designer**: Texas Instruments
* **Architecture Paradigm**: VLIW (Very Long Instruction Word) with Fetch Packets (P-bit)

## Overview
The Texas Instruments TMS320C6000 (C6000/C6x) is a high-performance digital signal processor (DSP) family using a VLIW architecture. It packages parallel instructions into fetch packets where a **parallel bit (p-bit)** in each instruction specifies whether it should be executed concurrently with the next instruction in the packet.

In this model:
- 32-bit data and addressing.
- General purpose registers R0-R15.
- Parallel instructions are marked with `||` prefix in assembly. In binary format, if the p-bit (bit 0) is set, the instruction executes in parallel with its successor.
- Execution units support:
  - **ALU Operations**: ADD, SUB.
  - **LSU Operations**: LD, ST, LD_CU.
  - **Control Operations**: JMP, HLT.

## Instruction Set Summary
- **ALU**: `ADD/SUB Rd, Rs1, Rs2`
- **LSU**: `LD/ST Reg, Base`
- **Control**: `JMP target` / `HLT`
- **Standalone Directives**: `LD_CU Rd, target` load register immediate pointer.
