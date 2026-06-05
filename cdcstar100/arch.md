# CDC STAR-100 Architecture Reference 🏛️

* **Design Year**: 1974
* **Designer**: Control Data Corporation (CDC)
* **Architecture Paradigm**: Memory-to-Memory Vector Processor

## Overview
The CDC STAR-100 (STring ARray) was one of the first commercial supercomputers to utilize vector processing. It employed a **memory-to-memory** vector architecture, meaning it didn't utilize vector registers; instead, vector instructions read stream operands directly from main memory and wrote results directly back to memory. 

This model excelled at processing extremely long vectors but suffered from significant pipeline startup latency (overhead) for short vectors, which inspired Seymour Cray to design the register-based Cray-1.

In this model:
- 32-bit scalar data, addressing, and instructions.
- 8 scalar GPRs (R0-R7) for base addresses and loop lengths.
- Stream arithmetic pipelines performing parallel operations directly on memory buffers.

## Instruction Set Summary
- **Vector Operations**:
  - `VADD Rc, Ra, Rb, Rlen` — Memory-to-memory vector addition: `C[i] = A[i] + B[i]` for `i` in `0` to `regs(Rlen)-1`. `Ra`, `Rb`, and `Rc` contain the starting memory addresses.
  - `VSUB Rc, Ra, Rb, Rlen` — Memory-to-memory vector subtraction.
- **Control & Scalar Operations**:
  - `LD_CU Rd, target` — Load address pointer into GPR `Rd`.
  - `JMP target` — Unconditional jump.
  - `HLT` — Halt execution.
