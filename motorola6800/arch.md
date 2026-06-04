# Motorola 6800 Architecture

- **Designers**: Tom Bennett
- **Year Introduced**: 1974

## Unique Features
- **Big-Endian Address Ordering**: The Motorola 6800 stores 16-bit address values in memory high-byte first, followed by the low-byte, which stands in direct contrast to the little-endian design of the Intel 8080A.
- **Dual Accumulators (A and B)**: Features two independent 8-bit accumulators, allowing more local data manipulation without memory spills.
- **Advanced Control Bus Integration**: Leveraged three-state control buses and a single +5V power supply, which simplified system designs relative to contemporary microprocessors that required multiple supply levels.

---

## Instruction Formats

Motorola 6800 instructions vary from 1 to 3 bytes long based on the addressing mode:

### 1-Byte Instructions (Inherent / Implied Addressing)
```text
 7            0
+--------------+
|    Opcode    |
+--------------+
```

### 3-Byte Instructions (Extended/Absolute Addressing)
```text
 7            0 7            0 7            0
+--------------+--------------+--------------+
|    Opcode    | Address High | Address Low  |
+--------------+--------------+--------------+
```

---

## Implemented Instructions
- **Data Movement**:
  - **LDAA addr** (`0xB6`): Load Accumulator A from absolute 16-bit memory address (extended addressing mode).
  - **STAA addr** (`0xB7`): Store Accumulator A to absolute 16-bit memory address (extended addressing mode).
- **Arithmetic**:
  - **ADDA addr** (`0xBB`): Add memory contents at 16-bit address to Accumulator A (extended addressing mode).
- **Control Flow**:
  - **WAI** (`0x3E`): Wait for Interrupt (acts as Halt in simulator).

---

## Unimplemented Instructions / Features
- **Accumulator B**: Accumulator B and operations referencing it are unimplemented.
- **Index Register (X) & Stack Pointer (SP)**: Operations using the 16-bit index register X (LDX, STX, INX, DEX) or stack pointer (push/pop instructions) are not implemented.
- **Other Addressing Modes**: Immediate, direct (zero page equivalent), indexed, and relative addressing modes are not simulated.
- **Branch and Subroutine Flow**: Conditional branches (BNE, BEQ, BSR) and jumps (JMP, RTS) are not supported.

## Architectural Design Purpose

Early industrial process instrumentation and low-cost microcomputer system control.

## Target Purpose Stress Program

```assembly
# Motorola 6800 Arithmetic: y = x + a
LDAA 20
ADDA 21
STAA 22
```
