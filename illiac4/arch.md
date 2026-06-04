# ILLIAC IV Architecture Reference 🏛️

* **Design Year**: 1972
* **Designer**: Daniel Slotnick (University of Illinois / Burroughs)
* **Architecture Paradigm**: SIMD Array Processor (Single Instruction, Multiple Data)

## Overview
ILLIAC IV was the pioneering massively parallel computer. It featured a central Control Unit (CU) that fetched instructions and broadcast them to an array of 64 Processing Elements (PEs) running in parallel. Each PE was a powerful 64-bit floating-point execution unit with its own local memory segment. PEs were organized in an 8x8 nearest-neighbor grid (torus) layout.

In the BBZoo implementation, we model:
- A central Control Unit (CU) with 4 index/address registers (R0-R3) and a Program Counter (PC).
- An array of 4 Processing Elements (PEs) (representing the SIMD array).
- High-performance 64-bit datapath processing within the PEs.
- Parallel nearest-neighbor torus shifting/routing (`ROUTE_PE_L`, `ROUTE_PE_R`).

## Registers

### Control Unit (CU)
- **PC**: 16-bit Program Counter.
- **R0-R3**: 64-bit address/index registers. R1 is used by default as the base pointer for parallel PE memory operations.

### Processing Elements (PEs)
- **A**: 64-bit local accumulator register.
- **B**: 64-bit local register.

## Instruction Set Summary
Instructions are 16-bit wide, and data values are 64-bit wide.

### Local Control Unit (CU) Instructions
- `HLT` (`0x0000`): Halt execution.
- `LD_CU rd, addr` (`0x1100` / `0x1000 | (rd << 8)`): Load a 64-bit value from address `addr` (following word) into CU register `rd`.
- `ST_CU rs, addr` (`0x2000 | (rs << 8)`): Store CU register `rs` to address `addr`.
- `LDI_CU rd, rs` (`0x3000 | (rd << 8) | (rs << 4)`): Load indirect from memory pointed by CU register `rs` into CU register `rd`.
- `STI_CU rs, rd` (`0x4000 | (rs << 8) | (rd << 4)`): Store indirect from CU register `rs` to memory pointed by CU register `rd`.
- `JNZ_CU rd, addr` (`0x5000 | (rd << 8)`): Jump to address `addr` if CU register `rd` is not zero.
- `JMP_CU addr` (`0x6000`): Jump to address `addr`.

### Parallel Processing Element (PE) Broadcast Instructions
These instructions are broadcast from the CU and executed in parallel across all PEs:
- `LD_PE` (`0x7000`): Load each PE's local accumulator `A` from its local memory slice at address `R1 + PE_index`.
- `ST_PE` (`0x8000`): Store each PE's local accumulator `A` into local memory at address `R1 + PE_index`.
- `ADD_PE` (`0x9000`): Add memory value at `R1 + PE_index` to PE accumulator `A`.
- `ROUTE_PE_L` (`0xA000`): Shift all PE accumulators left: `PE[i].A := PE[i+1].A` (with wrap-around).
- `ROUTE_PE_R` (`0xB000`): Shift all PE accumulators right: `PE[i].A := PE[i-1].A` (with wrap-around).
- `ADD_PE_REG` (`0xC000`): Add PE local register `B` to PE local accumulator `A`.
