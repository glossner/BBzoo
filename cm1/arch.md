# Thinking Machines CM-1 Architecture Reference 🏛️

* **Design Year**: 1985
* **Designer**: Danny Hillis (Thinking Machines Corporation)
* **Architecture Paradigm**: Massive Bit-Serial SIMD with Hypercube Routing

## Overview
The Connection Machine CM-1 was a massive SIMD supercomputer that could house up to 65,536 1-bit processing elements. It combined bit-serial arithmetic with a custom packet-switched hypercube routing network that allowed any processor to communicate with any other.

In this model:
- A central Control Unit (CU) with PC and 4 index registers (R0-R3).
- 4 PEs, representing a 2D hypercube network.
- Hypercube routing instruction `ROUTE_CM1 dim` swapping PE accumulator registers across hypercube dimensions (dimension 0: 0<->1, 2<->3; dimension 1: 0<->2, 1<->3).
- Bit-serial 16-bit vector operations.

## Instruction Set Summary
Instructions are 16-bit wide, and data values are 16-bit wide.

### Control Unit (CU) Instructions
- `HLT` (`0x0000`): Halt execution.
- `LD_CU rd, addr` (`0x1100` / `0x1000 | (rd << 8)`): Load immediate address pointer into `rd`.
- `ST_CU rs, addr` (`0x2000 | (rs << 8)`): Store CU register.

### Parallel Processing Element (PE) Broadcast Instructions
- `LD_PE_BIT bit` (`0x3000 | bit`): Load bit `bit` of local memory word at `R1 + PE_index` into PE accumulator `A`.
- `ADD_PE_BIT bit` (`0x4000 | bit`): Add bit `bit` of memory word at `R1 + PE_index` plus carry `C` to accumulator `A`, updating carry.
- `ST_PE_BIT bit` (`0x5000 | bit`): Store accumulator `A` into bit `bit` of memory word at `R1 + PE_index`.
- `CLR_CARRY` (`0x6000`): Clear carry `C` to 0 in all PEs.
- `ROUTE_CM1 dim` (`0x7000 | dim`): Swap PE accumulator `A` across hypercube dimension `dim` (0 or 1).
