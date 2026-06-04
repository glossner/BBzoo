# MOS 6502 Architecture

- **Designers**: Chuck Peddle, Albert Organ, and former Motorola engineers (for MOS Technology)
- **Year Introduced**: 1975

## Unique Features
- **Cost-Optimized Design**: Replaced many general-purpose registers with a minimal set: Accumulator ($A$), Index Registers ($X$ and $Y$), Stack Pointer ($S$), Status Register ($P$), and PC.
- **Zero Page Addressing**: Memory addresses in the first 256 bytes ($0x00$-$0xFF$) can be accessed using dense 2-byte instructions (opcode + 8-bit address) instead of 3-byte absolute instructions, effectively treating memory as a 256-word register file.
- **Flexible Address Modes**: Possesses a wide variety of indexing modes (e.g., indexed absolute, indexed indirect, and indirect indexed).
- **Decimal Mode Flag (D)**: Has a hardware control flag that dynamically switches arithmetic instructions (ADC, SBC) to operate in Binary Coded Decimal (BCD).

---

## Instruction Formats

MOS 6502 instructions are 1, 2, or 3 bytes long. The layout depends on the addressing mode:

### 1-Byte Instructions (Implied / Single-byte)
```text
 7            0
+--------------+
|    Opcode    |
+--------------+
```

### 2-Byte Instructions (Immediate / Zero Page / Relative)
```text
 7            0 7            0
+--------------+--------------+
|    Opcode    |  8-bit Data  |
+--------------+--------------+
```

### 3-Byte Instructions (Absolute / Absolute Indexed)
```text
 7            0 7            0 7            0
+--------------+--------------+--------------+
|    Opcode    |   Low Byte   |  High Byte   |
+--------------+--------------+--------------+
```
- Multi-byte addresses are stored in little-endian format (low byte first, then high byte).

---

## Implemented Instructions
- **Data Movement**:
  - **LDA #imm** (0xA9): Load Accumulator with immediate value.
  - **LDA abs** (0xAD): Load Accumulator from absolute memory address.
  - **STA abs** (0x8D): Store Accumulator to absolute memory address.
- **Arithmetic**:
  - **ADC #imm** (0x69): Add immediate to Accumulator with Carry.
  - **ADC abs** (0x6D): Add memory value to Accumulator with Carry.
- **Status Register**:
  - **CLC** (0x18): Clear Carry flag.
- **Control Flow**:
  - **BRK** (0x00): Software interrupt (acts as Halt in simulator).

---

## Unimplemented Instructions / Features
- **Index Registers (X, Y)**: General index registers $X$ and $Y$ and related instructions (LDX, LDY, STX, STY, INX, DEX, INY, DEY) are not implemented.
- **Index Addressing Modes**: Zero-page indexed, absolute indexed, indexed indirect, and indirect indexed addressing modes are not supported.
- **Decimal Mode**: Hardware BCD arithmetic is not simulated.
- **Subroutines & Stack**: Subroutine call (`JSR`), return (`RTS`), interrupt return (`RTI`), and stack push/pop instructions (`PHA`, `PLA`, `PHP`, `PLP`) are not implemented.

## Architectural Design Purpose

Cost-sensitive consumer home microcomputing and video game console processing using zero-page registers.

## Target Purpose Stress Program

```assembly
# MOS 6502 Arithmetic: y = x + a
LDA 20
CLC
ADC 21
STA 22
```
