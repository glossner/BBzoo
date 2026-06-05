# NEC SX-2 Architecture Reference 🏛️

* **Design Year**: 1983
* **Designer**: NEC (Nippon Electric Company)
* **Architecture Paradigm**: Vector-Register Architecture with Vector-Scalar operations

## Overview
The NEC SX-2 was a world-class vector supercomputer representing the peak of high-throughput scientific computing. It incorporated multiple parallel arithmetic pipelines, a large vector-register file, and introduced advanced hardware support for **vector-scalar** arithmetic (e.g. adding a single scalar directly to all elements of a vector register).

In this model:
- 32-bit scalar data, addressing, and instructions.
- 8 GPRs (R0-R7) for scalar arithmetic.
- 4 vector registers (V0-V3), each holding up to 4 elements (32-bit).
- Vector Length (VL) register.

## Instruction Set Summary
- **Vector Load and Store**:
  - `VLD Vi, Rs` — Load vector register `Vi` from memory address in GPR `Rs`.
  - `VST Vi, Rs` — Store vector register `Vi` to memory starting at address in GPR `Rs`.
- **Vector Operations**:
  - `VADD Vk, Vi, Vj` — Element-wise vector addition: `Vk[i] = Vi[i] + Vj[i]`.
  - `VSADD Vk, Vi, Rs` — Vector-Scalar addition: `Vk[i] = Vi[i] + regs(Rs)` (adds scalar register `Rs` to each element of `Vi`).
- **Control & Scalar**:
  - `SETVL Rs` — Set Vector Length (VL).
  - `LD_CU Rd, target` — Load scalar address pointer.
  - `JMP target` — Unconditional jump.
  - `HLT` — Halt.
