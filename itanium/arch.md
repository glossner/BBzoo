# Intel Itanium Architecture Reference 🏛️

* **Design Year**: 2001
* **Designer**: Intel / HP
* **Architecture Paradigm**: EPIC (Explicitly Parallel Instruction Computing) / VLIW

## Overview
The Intel Itanium is a 64-bit microprocessor utilizing the Explicitly Parallel Instruction Computing (EPIC) architecture. The design relies on the compiler to explicitly group independent instructions into 128-bit bundles containing three 41-bit instruction slots and a 5-bit template field. Parallel bundles are bounded by **stop bits (;;)** which instruct the hardware to stall subsequent instructions until the current group finishes.

In this model:
- 64-bit data and 32-bit addressing.
- General purpose registers R0-R15.
- 128-bit EPIC bundles contain:
  - **Slot 0**: 32-bit instruction slot.
  - **Slot 1**: 32-bit instruction slot.
  - **Slot 2**: 32-bit instruction slot.
  - **Template**: 32-bit word specifying execution unit routing and stop bits (`;;`).

## Instruction Set Summary
- **ALU**: `ADD/SUB Rd, Rs1, Rs2`
- **LSU**: `LD8/ST8 Reg, [Base]`
- **Control**: `JMP target` / `HLT`
- **Standalone Directives**: `LD_CU Rd, target` load register immediate pointer.
