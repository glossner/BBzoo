# Transmeta Crusoe Architecture Reference 🏛️

* **Design Year**: 2000
* **Designer**: David Ditzel / Transmeta team
* **Architecture Paradigm**: VLIW (Very Long Instruction Word) with Code Morphing Engine

## Overview
The Transmeta Crusoe was an innovative microprocessor designed to run x86 code with low power consumption. Instead of executing x86 instructions directly in hardware, it used a software layer called the **Code Morphing Software** to translate sequential target instructions dynamically into parallel VLIW bundles ("molecules") that executed on the core.

In this model:
- 32-bit data and addressing.
- General purpose registers R0-R15.
- Sequential front-end instructions are decoded and mapped to Crusoe's internal execution slots (ALU, LSU, CTRL) by the Morph Engine buffer before execution.
- Instruction slots include:
  - **ALU**: ADD, SUB, or NOP.
  - **LSU**: LD, ST, or NOP.
  - **CTRL**: JMP, HLT, or NOP.

## Instruction Set Summary
- **ALU**: `ADD/SUB Rd, Rs1, Rs2`
- **LSU**: `LD/ST Reg, Base`
- **Control**: `JMP target` / `HLT`
- **Standalone Directives**: `LD_CU Rd, target` load register immediate pointer.
