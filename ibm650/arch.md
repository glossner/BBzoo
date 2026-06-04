# IBM 650 Architecture

- **Designer**: Frank E. Hamilton, Ernest S. Hughes Jr., James W. Birkenstock (at IBM)
- **Year Introduced**: 1953

## Unique Features
- **Magnetic Drum Memory**: Primary storage utilized a magnetic drum rotating at 12,500 RPM, holding 1,000 to 2,000 10-digit words.
- **Bi-Address Instruction Format**: Lacked a conventional sequential program counter. Instead, every instruction encoded both a data address and the address of the *next* instruction. This enabled "optimal programming," allowing the programmer to schedule execution timing in sync with the rotating drum storage.
- **Bi-Quinary Internal Representation**: Extensively used bi-quinary coding decimal representations internally for automatic hardware error-checking.
- **10-Digit Decimal Words**: Operated on words containing 10 decimal digits plus sign.

---

## Instruction Formats

In this simulator, each 40-bit word is structured as:
```text
 39          32 31          16 15                 0
+--------------+--------------+--------------------+
|    Opcode    |  Data Addr   |     Next Addr      |
+--------------+--------------+--------------------+
```
- **Opcode** (bits 32-39): 8-bit instruction opcode.
- **Data Addr** (bits 16-31): 16-bit target memory location for the operand.
- **Next Addr** (bits 0-15): 16-bit location of the next instruction to fetch.

---

## Implemented Instructions
- **LD data_addr, next_addr** (0x01): Load value from `data_addr` into Accumulator, then branch to `next_addr`.
- **ADD data_addr, next_addr** (0x02): Add value at `data_addr` to Accumulator, then branch to `next_addr`.
- **ST data_addr, next_addr** (0x03): Store Accumulator value to `data_addr`, then branch to `next_addr`.
- **HLT** (0x04): Halt processor execution.

---

## Unimplemented Instructions / Features
- **Bi-Quinary Hardware Checks**: The simulated Chisel core operates on binary representation and does not model bi-quinary error flags.
- **Hardware Multiply & Divide**: Full 20-digit double accumulator arithmetic operations are not implemented.
- **Magnetic Drum Rotational Latency**: Drum read/write head seek times are not modeled; memory accesses resolve in a single clock cycle.
