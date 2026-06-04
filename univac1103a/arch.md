# UNIVAC 1103A Architecture

- **Designer**: Seymour Cray
- **Year Introduced**: 1956

## Unique Features
- **Two-Address Logic**: A major scientific computer executing operations directly on two memory operands `u` and `v` within a single instruction, avoiding single-accumulator load/store bottlenecks.
- **Magnetic Core Memory**: First model in the UNIVAC 1103 family to replace unstable Williams tube electrostatic memory with reliable magnetic core memory.
- **True Hardware Interrupt System**: First commercial machine to implement a dedicated hardware interrupt signal.

---

## Instruction Formats

The UNIVAC 1103A uses a 36-bit instruction format:
```text
 35        30 29              15 14              0
+------------+------------------+------------------+
|   Opcode   |     u address    |     v address    |
+------------+------------------+------------------+
```
- **Opcode** (bits 30-35): 6-bit instruction operation code.
- **u address** (bits 15-29): 15-bit source operand address.
- **v address** (bits 0-14): 15-bit target operand address.

---

## Implemented Instructions
- **TP u, v** (opcode 11): Transmit Positive: copies value at memory address `u` to memory address `v`.
- **ADD u, v** (opcode 12): Add: adds `mem(u)` to `mem(v)` and stores the result at memory address `v`.
- **SUB u, v** (opcode 13): Subtract: subtracts `mem(u)` from `mem(v)` and stores the result at memory address `v`.
- **HLT** (opcode 14): Halts the processor.

---

## Unimplemented Instructions / Features
- **Repeat Directive (RPT)**: Repetitive execution modifiers utilizing repetition counter fields are not simulated.
- **Subtractive 72-bit Accumulator**: Standard multi-precision subtractive accumulator operations are not implemented.
- **Floating-point registers**: Hardware floating point representation (characteristic and mantissa) is not simulated.

## Architectural Design Purpose

Aerospace simulation, cryptanalysis, and military scientific calculation using memory-to-memory two-address instructions.

## Target Purpose Stress Program

```assembly
# Univac 1103A Polynomial evaluation: y = (x + a) - b
TP 20, 23    # Transmit Positive (copy x to y)
ADD 21, 23   # Add a to y
SUB 22, 23   # Subtract b from y
HLT
```
