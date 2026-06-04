# Goodyear MPP Architecture Reference 🏛️

* **Design Year**: 1983
* **Designer**: Kenneth Batcher (Goodyear Aerospace / NASA)
* **Architecture Paradigm**: Bit-Serial SIMD Array Processor with Masking

## Overview
The Goodyear Massively Parallel Processor (MPP) was designed for ultra-high speed processing of satellite imagery. It had an array of 128x128 PEs. A key feature of MPP PEs was the `G` mask register (activity/enable register) that enabled conditional execution and write masking.

In this model:
- A central Control Unit (CU) with PC and 4 index registers (R0-R3).
- 4 PEs, each with a 1-bit accumulator `A`, a 1-bit carry `C`, and a 1-bit mask register `G`.
- Bit-serial 16-bit vector operations, where writes are masked (gated) by each PE's `G` register.

## Instruction Set Summary
Instructions are 16-bit wide, and data values are 16-bit wide.

### Control Unit (CU) Instructions
- `HLT` (`0x0000`): Halt execution.
- `LD_CU rd, addr` (`0x1100` / `0x1000 | (rd << 8)`): Load immediate address pointer into `rd`.
- `ST_CU rs, addr` (`0x2000 | (rs << 8)`): Store CU register.

### Parallel Processing Element (PE) Broadcast Instructions
- `LD_PE_BIT bit` (`0x3000 | bit`): Load bit `bit` of local memory word at `R1 + PE_index` into PE accumulator `A`.
- `ADD_PE_BIT bit` (`0x4000 | bit`): Add bit `bit` of memory word at `R1 + PE_index` plus carry `C` to accumulator `A`, updating carry.
- `ST_PE_BIT bit` (`0x5000 | bit`): Store accumulator `A` into bit `bit` of memory word at `R1 + PE_index` if PE mask `G` is 1.
- `CLR_CARRY` (`0x6000`): Clear carry `C` to 0 in all PEs.
- `LD_PE_MASK` (`0x7000`): Load PE mask `G` from LSB of memory word at `R1 + PE_index`.
