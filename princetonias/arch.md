# Princeton IAS Architecture

- **Designer**: John von Neumann and team (at the Institute for Advanced Study, Princeton)
- **Year Completed**: 1952

## Unique Features
- **The Prototype Von Neumann Machine**: Built as the operational model of the "stored-program" concept, influencing almost all subsequent computer designs.
- **40-bit Word Structure**: Operates on 40-bit words representing binary integers.
- **Packed Instruction Format**: Standard design packed two 20-bit instructions (8-bit opcode + 12-bit address) into each 40-bit word.
- **AC and MQ Registers**: Possessed an Accumulator ($AC$) and a Multiplier-Quotient ($MQ$) register for intermediate arithmetic operations.

---

## Instruction Formats

In this simulator, the 40-bit instruction word is formatted as:
```text
 39          12 11                 0
+--------------+--------------------+
|    Opcode    |      Address       |
+--------------+--------------------+
```
- **Opcode** (bits 12-39): 8-bit instruction opcode.
- **Address** (bits 0-11): 12-bit target memory location.

---

## Implemented Instructions
- **LD addr** (0x01): Load value from memory address `addr` into Accumulator $AC$.
- **ST addr** (0x02): Store current value of Accumulator $AC$ into memory address `addr`.
- **ADD addr** (0x03): Add value at memory address `addr` to Accumulator $AC$.
- **SUB addr** (0x04): Subtract value at memory address `addr` from Accumulator $AC$.
- **HLT** (0x05): Halt processor.

---

## Unimplemented Instructions / Features
- **Instruction Unpacking**: The instruction execution does not fetch and run two separate 20-bit instructions per word (each 40-bit word contains a single instruction).
- **Multiplier-Quotient (MQ) Register**: Multiplication, division, and MQ register transfers are not simulated.
- **Shift Operations**: Bitwise shifts of Accumulator values are not implemented.

## Architectural Design Purpose

Solving scientific, meteorological, and defense-related mathematical models using a parallel binary accumulator architecture.

## Target Purpose Stress Program

```assembly
# Princeton IAS Polynomial Evaluation: y = (x + a) - b
LD 20
ADD 21
SUB 22
ST 23
HLT
```
