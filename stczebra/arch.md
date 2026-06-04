# STC ZEBRA Architecture

- **Designer**: Willem van der Poel
- **Year Introduced**: 1958

## Unique Features
- **Functional Bits (Micro-operation Level Programming)**: Lacks traditional opcodes. The 15-bit operation field acts as direct control signals for logic gates, allowing programmers to compose custom operations (e.g. clearing accumulator, reading/writing memory, and performing additions simultaneously) in a single instruction word.
- **Extreme Minimization**: Designed to use the absolute minimum number of vacuum tubes to maximize reliability.
- **Recirculating Drum Registers**: Storage registers and accumulators are implemented directly as tracks on a magnetic drum.

---

## Instruction Formats

The STC ZEBRA utilizes a 33-bit instruction word layout:
```text
 32 31 30 29 28 27                  18 17                 5 4        0
+--+--+--+--+--+----------------------+--------------------+----------+
|C |R |W |A |H |        Unused        |     Drum Address   | Reg Address|
+--+--+--+--+--+----------------------+--------------------+----------+
```
- **C** (bit 32): Clear Accumulator before execution.
- **R** (bit 31): Read from memory.
- **W** (bit 30): Write to memory.
- **A** (bit 29): Add memory operand to Accumulator (if R is set).
- **H** (bit 28): Halt processor.
- **Drum Address** (bits 5-17): 13-bit address of the drum location.
- **Register Address** (bits 0-4): 5-bit address for register indexing (not simulated).

---

## Implemented Instructions
- **LD addr** (C, R, A set): Clears accumulator and reads/adds memory at `addr` to it, performing a clean load.
- **ADD addr** (R, A set): Reads and adds memory at `addr` to accumulator.
- **ST addr** (W set): Writes accumulator to memory at `addr`.
- **HLT** (H set): Halts the processor.

---

## Unimplemented Instructions / Features
- **Register Indexing**: Indexing operations using the 5-bit register field are not simulated.
- **Rotations and Shifts**: Bit-level shift and rotate operations are not implemented.
- **Drum Latency Optimization**: Optimum programming timing simulator for drum revolution optimization is not implemented.
