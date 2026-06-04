# ICL DAP Architecture Reference 🏛️

* **Design Year**: 1979
* **Designer**: Stewart Reddaway (ICL)
* **Architecture Paradigm**: Bit-Serial SIMD Array Processor

## Overview
The ICL Distributed Array Processor (DAP) was a pioneering massively parallel bit-serial processor. It contained an array of Processing Elements (PEs) (historically 32x32 or 64x64) controlled by a central Control Unit. Each PE worked on 1-bit registers (Accumulator and Carry) and processed arithmetic bit-by-bit sequentially.

In this model:
- A central Control Unit (CU) with PC and 4 index registers (R0-R3).
- 4 PEs, each with a 1-bit accumulator `A` and a 1-bit carry `C`.
- Bit-serial 16-bit vector operations executing sequentially over 16 bit-level broadcast steps.

## Instruction Set Summary
Instructions are 16-bit wide, and data values are 16-bit wide.

### Control Unit (CU) Instructions
- `HLT` (`0x0000`): Halt execution.
- `LD_CU rd, addr` (`0x1100` / `0x1000 | (rd << 8)`): Load immediate address pointer into `rd`.
- `ST_CU rs, addr` (`0x2000 | (rs << 8)`): Store CU register.

### Parallel Processing Element (PE) Broadcast Instructions
- `LD_PE_BIT bit` (`0x3000 | bit`): Load bit `bit` of local memory word at `R1 + PE_index` into PE accumulator `A`.
- `ADD_PE_BIT bit` (`0x4000 | bit`): Add bit `bit` of memory word at `R1 + PE_index` plus carry `C` to accumulator `A`, updating carry.
- `ST_PE_BIT bit` (`0x5000 | bit`): Store accumulator `A` into bit `bit` of memory word at `R1 + PE_index` (using read-modify-write).
- `CLR_CARRY` (`0x6000`): Clear carry `C` to 0 in all PEs.
