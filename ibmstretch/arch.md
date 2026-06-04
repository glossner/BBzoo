# IBM Stretch (IBM 7030) Architecture

- **Designer**: Stephen Dunwell
- **Year Introduced**: 1961

## Unique Features
- **Pipelining and Lookahead**: One of the earliest systems to design a multi-stage instruction prefetch pipeline (fetching, decoding, and effective address computation in parallel with arithmetic execution).
- **Index Registers**: Features 16 dedicated 64-bit index registers (`X0`..`X15`) allowing address modification at hardware speed.
- **Bit and Word Addressing**: Supported addressing memory at both the word-level and the individual bit-level to allow highly flexible variable-length fields (influencing subsequent byte-based architectures like the IBM System/360).

---

## Instruction Formats

The IBM Stretch instruction format represented in this simple 64-bit core is:
```text
 63          56 55      52 51                               0
+--------------+----------+----------------------------------+
|    Opcode    |Index Reg |             Address              |
+--------------+----------+----------------------------------+
```
- **Opcode** (bits 56-63): 8-bit instruction opcode.
- **Index Reg** (bits 52-55): 4-bit index register selection (`X0`..`X15`).
- **Address** (bits 0-19): 20-bit target address or immediate value (for index load/add).

---

## Implemented Instructions
- **LD ACC, addr(Xreg)** (opcode 1): Loads accumulator `ACC` with value from `addr + Xreg`.
- **ADD ACC, addr(Xreg)** (opcode 2): Adds value from `addr + Xreg` to accumulator `ACC`.
- **ST ACC, addr(Xreg)** (opcode 3): Stores accumulator `ACC` value to `addr + Xreg`.
- **LDX Xreg, value** (opcode 4): Loads index register `Xreg` with immediate `value`.
- **ADDX Xreg, value** (opcode 5): Adds immediate `value` to index register `Xreg`.
- **HLT** (opcode 6): Halts the processor.

---

## Unimplemented Instructions / Features
- **Bit-level memory addressing**: True bit-addressable memory offsets and variable byte sizes are not implemented.
- **Out-of-order execution lookahead**: The lookahead buffer and instruction completion buffers are not simulated.
- **Floating Point ALU**: Hardware floating-point operations and formats are not modeled.
