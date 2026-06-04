# IBM 704 Architecture

- **Designer**: Gene Amdahl and team (at IBM)
- **Year Introduced**: 1954

## Unique Features
- **First Mass-Produced Computer with Floating-Point Hardware**: Introduced standard hardware floating-point math, transforming scientific computation.
- **First to Use Magnetic Core Memory**: Shifted from Williams tube electrostatic memory to much more reliable magnetic core memory.
- **Introduction of Index Registers**: Featured three 15-bit index registers, which allowed automatic address modification during loop executions.
- **36-bit Word / 15-bit Address Width**: Used a 36-bit word length and a 15-bit address space, allowing addressing up to 32,768 words.

---

## Instruction Formats

In this simulator, each instruction is formatted as:
```text
 35          27 26          15 14                 0
+--------------+--------------+--------------------+
|    Unused    |    Opcode    |      Address       |
+--------------+--------------+--------------------+
```
- **Opcode** (bits 15-26): 12-bit instruction opcode.
- **Address** (bits 0-14): 15-bit target memory location (corresponds to the 15-bit address space of the physical machine).
- **Unused** (bits 27-35): Left as zero.

---

## Implemented Instructions
- **LD addr** (0x01): Load value from memory address `addr` into Accumulator.
- **ST addr** (0x02): Store Accumulator value to memory address `addr`.
- **ADD addr** (0x03): Add value at memory address `addr` to Accumulator.
- **SUB addr** (0x04): Subtract value at memory address `addr` from Accumulator.
- **HLT** (0x05): Halt processor.

---

## Unimplemented Instructions / Features
- **Index Registers**: Real index register operations (`TXI`, `TIX`, etc.) and indexing address modification are not simulated.
- **Hardware Floating-Point**: Floating-point instructions (`FAD`, `FSB`, `FMP`, `FDH`, etc.) are not simulated.
- **Multiplier-Quotient (MQ) Register**: Multiplication, division, and MQ register transfers are not simulated.
- **Bitwise Shifts & Logical Operations**: Omitted from the simulator.
