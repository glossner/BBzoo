# TI ASC Architecture Reference 🏛️

* **Design Year**: 1972
* **Designer**: Texas Instruments (TI)
* **Architecture Paradigm**: Memory-to-Memory Vector Processor

## Overview
The Texas Instruments Advanced Scientific Computer (TI ASC) was a pioneering memory-to-memory vector supercomputer designed for seismic, weather, and scientific processing. It featured up to four pipelined arithmetic units (pipes) and integrated a highly advanced memory control unit to stream operands from main memory directly into the calculation pipelines.

Like the STAR-100, it bypassed vector registers in favor of memory streaming, but featured a highly flexible Vector Parameter File (VPF) mechanism to configure strides and multi-dimensional loops.

In this model:
- 32-bit scalar data, addressing, and instructions.
- 8 scalar GPRs (R0-R7).
- Multi-pipe scientific execution units.

## Instruction Set Summary
- **Vector Operations**:
  - `VADD_ASC Rc, Ra, Rb, Rlen` — Memory-to-memory vector addition: `C[i] = A[i] + B[i]` for `i` in `0` to `regs(Rlen)-1`.
  - `VSUB_ASC Rc, Ra, Rb, Rlen` — Memory-to-memory vector subtraction.
- **Control & Scalar Operations**:
  - `LD_CU Rd, target` — Load address pointer into GPR `Rd`.
  - `JMP target` — Unconditional jump.
  - `HLT` — Halt execution.
