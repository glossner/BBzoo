# IBM System/370 Vector Facility Architecture Reference 🏛️

* **Design Year**: 1985
* **Designer**: IBM
* **Architecture Paradigm**: Vector-Register Mainframe Extension

## Overview
The IBM System/370 Vector Facility (S/370 VF) was a mainframe extension that added high-performance vector processing capabilities to IBM's standard general-purpose computing systems. Rather than requiring a separate supercomputer, programmers could execute vector instructions as a tightly integrated coprocessor directly on the S/370 mainframe.

It introduced vector registers and a dedicated **Vector Count (VCT)** register (which acts as the vector length register) to S/370's architectural state.

In this model:
- 32-bit scalar data, addressing, and instructions.
- 8 scalar GPRs (R0-R7) representing S/370 general registers.
- 4 vector registers (V0-V3), each holding up to 4 elements (32-bit).
- Vector Count (VCT) register.

## Instruction Set Summary
- **Vector Load and Store**:
  - `VLD Vi, Rs` — Load vector register `Vi` from memory address in GPR `Rs`.
  - `VST Vi, Rs` — Store vector register `Vi` to memory starting at address in GPR `Rs`.
- **Vector Arithmetic**:
  - `VADD Vk, Vi, Vj` — Element-wise vector addition: `Vk[i] = Vi[i] + Vj[i]`.
  - `VSUB Vk, Vi, Vj` — Element-wise vector subtraction.
- **Control & Scalar**:
  - `VLVC Rs` — Load Vector Count (VCT) from GPR `Rs` (equivalent to SETVL).
  - `LD_CU Rd, target` — Load scalar address pointer.
  - `JMP target` — Unconditional jump.
  - `HLT` — Halt.
