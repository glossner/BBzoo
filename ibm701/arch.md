# IBM 701 Architecture

- **Designer**: Nathaniel Rochester and team (at IBM)
- **Year Introduced**: 1952

## Unique Features
- **IBM's First Commercial Scientific Computer**: Known as the "Defense Calculator," it was IBM's first mass-produced electronic stored-program computer.
- **36-bit Word / Half-word Addressing**: Primarily utilized 36-bit full words but supported addressing memory as 18-bit half-words for instruction storage.
- **Electrostatic Storage Tubes**: Utilized Williams tubes (electrostatic memory) for primary memory storage, which were later replaced by magnetic core memory in subsequent models.

---

## Instruction Formats

In this simulator, each instruction is formatted as:
```text
 35          18 17          12 11                 0
+--------------+--------------+--------------------+
|    Unused    |    Opcode    |      Address       |
+--------------+--------------+--------------------+
```
- **Opcode** (bits 12-17): 6-bit instruction opcode.
- **Address** (bits 0-11): 12-bit target memory location.
- **Unused** (bits 18-35): Left as zero.

---

## Implemented Instructions
- **LD addr** (0x01): Load value from memory address `addr` into Accumulator.
- **ST addr** (0x02): Store Accumulator value to memory address `addr`.
- **ADD addr** (0x03): Add value at memory address `addr` to Accumulator.
- **SUB addr** (0x04): Subtract value at memory address `addr` from Accumulator.
- **HLT** (0x05): Halt processor.

---

## Unimplemented Instructions / Features
- **Half-word Addressing**: The simulator operates strictly on 36-bit full words and does not support half-word addressing modes or instruction packing.
- **Multiplier-Quotient (MQ) Register**: Multiplication, division, and MQ transfers are not simulated.
- **Index Registers**: Unlike the IBM 704, index registers and indexing options are not present on the IBM 701.
- **Shift Operations**: Bitwise shifts of accumulator values are omitted.
- **Electrostatic Memory Timing**: Williams tube refresh and cycle delays are not modeled.

## Architectural Design Purpose

Large-scale scientific modeling, defense calculations, and military cryptanalysis.

## Target Purpose Stress Program

```assembly
# IBM 701 Polynomial Evaluation: y = (x + a) - b
LD 20
ADD 21
SUB 22
ST 23
HLT
```
