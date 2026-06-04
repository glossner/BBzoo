# Motorola 68000 Architecture

- **Designer**: Motorola Semiconductor Products Sector (led by Thomas Gunter)
- **Year Introduced**: 1979

## Unique Features
- **16/32-Bit Hybrid Design**: Has 32-bit internal registers and data paths, but historically interfaces with a 16-bit external data bus.
- **Differentiated Address (An) and Data (Dn) Registers**: Separates general computation registers ($D0$-$D7$) from memory pointer address registers ($A0$-$A7$).
- **Rich Address Modes**: Designed with orthogonal addressing modes including register direct, address register indirect, post-increment, pre-decrement, displacement, and indexed modes.
- **Variable Instruction Lengths**: Opcodes are 16-bit, but can be followed by 16-bit or 32-bit extension words representing absolute memory locations, immediate values, or displacements.

---

## Instruction Formats

### Move Instruction (MOVE.L / MOVEA.L)
```text
 15  14 13    12 11     9 8       6 5       3 2       0
+------+--------+---------+---------+---------+---------+
|  00  |  Size  | Dst Reg | Dst Mode| Src Mode| Src Reg |
+------+--------+---------+---------+---------+---------+
```
- **Size**: 11 = Longword (32-bit) operation.
- **Dst Reg / Dst Mode**: Destination register and addressing mode.
- **Src Reg / Src Mode**: Source register and addressing mode.

### Arithmetic Instruction (ADD.L / SUB.L)
```text
 15      12 11     9 8     6 5                   0
+----------+--------+-------+---------------------+
|  Opcode  | Dst Reg| Mode  |     Src Register    |
+----------+--------+-------+---------------------+
```
- **Opcode**: 0xD = ADD, 0x9 = SUB.
- **Mode**: 010 = Longword destination register direct.

### Branch Instruction (BRA)
```text
 15            8 7                               0
+---------------+---------------------------------+
|     0x60      |        8-bit Displacement       |
+---------------+---------------------------------+
```

---

## Implemented Instructions
- **Data Movement**:
  - **MOVE.L**: Move 32-bit longword from source (Dn, An, indirect, or Immediate) to destination (Dn, indirect).
  - **MOVEA.L**: Move 32-bit longword from source to Address register ($A0$-$A7$).
- **Arithmetic**:
  - **ADD.L Dm, Dn**: Add data register $Dm$ to data register $Dn$.
  - **SUB.L Dm, Dn**: Subtract data register $Dm$ from data register $Dn$.
- **Control Flow**:
  - **BRA**: Branch unconditionally using an 8-bit signed PC-relative offset.

---

## Unimplemented Instructions / Features
- **Size Modifiers**: Byte (.B) and Word (.W) instructions are not implemented (only .L is supported).
- **Complex Addressing Modes**: Post-increment ($An+$), pre-decrement ($-An$), index addressing, and absolute address addressing are not implemented.
- **Multiply/Divide**: `MULU`, `MULS`, `DIVU`, `DIVS` arithmetic operations are not implemented.
- **Bitwise Logic**: `AND`, `OR`, `EOR`, `NOT`, and bit tests (`BTST`, `BSET`) are not implemented.
- **Status Register (SR) / User Stack (USP)**: Processor flags (CCR), system flags, and supervisor states are not simulated.
