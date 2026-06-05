# Convex C1 Architecture Reference 🏛️

* **Design Year**: 1985
* **Designer**: Convex Computer Corporation
* **Architecture Paradigm**: Vector-Register (Register-to-Register) Architecture

## Overview
The Convex C1 was the defining "mini-supercomputer," bringing Cray-like vector-register processing to departmental computing at a fraction of supercomputer costs. It integrated a virtual memory architecture and ran a Unix-based operating system, simplifying vector programming via advanced vectorizing compilers.

In this model:
- 32-bit scalar data, addressing, and instructions.
- 8 general-purpose registers (R0-R7) for scalar arithmetic and address base pointers.
- 4 vector registers (V0-V3), each holding up to 4 elements (32-bit).
- Vector Length (VL) register controlling the active number of elements in vector instructions.

## Instruction Set Summary
- **Vector Load and Store**:
  - `VLD Vi, Rs` — Load vector register `Vi` starting from memory address in GPR `Rs`.
  - `VST Vi, Rs` — Store vector register `Vi` to memory starting at address in GPR `Rs`.
- **Vector Arithmetic**:
  - `VADD Vk, Vi, Vj` — Element-wise vector addition: `Vk[i] = Vi[i] + Vj[i]` for `i` in `0` to `VL-1`.
  - `VSUB Vk, Vi, Vj` — Element-wise vector subtraction.
- **Control & Scalar**:
  - `SETVL Rs` — Set Vector Length (VL) from GPR `Rs`.
  - `LD_CU Rd, target` — Load scalar address pointer.
  - `JMP target` — Unconditional jump.
  - `HLT` — Halt.
