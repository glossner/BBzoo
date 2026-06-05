# IBM 801 Architecture

- **Designers**: John Cocke and the IBM Team
- **Year Introduced**: 1980

## Unique Features
- **Pioneering RISC Design**: The IBM 801 is widely recognized as the first true RISC (Reduced Instruction Set Computer) processor, laying the foundation for all subsequent RISC architectures.
- **32-bit Architecture**: Employs a 32-bit datapath, 32-bit memory address space, and 32 general-purpose 32-bit registers ($R0$ through $R31$).
- **Strict Load/Store Paradigm**: Separates memory access from data operations, ensuring arithmetic only operates on registers.

---

## Instruction Formats

IBM 801 instructions are 32-bit words, with absolute address parameters occupying a second word:

### 1-Word Instructions (Register-to-Register and Halts)
```text
 31         26 25     21 20     16 15     11 10        0
+-------------+---------+---------+---------+-----------+
|   Opcode    |  Reg D  |  Reg S1 |  Reg S2 |  Unused   |
+-------------+---------+---------+---------+-----------+
```

### 2-Word Instructions (Load and Store absolute)
```text
 Word 1:
 31         26 25     21 20     16 15                 0
+-------------+---------+---------+---------------------+
|   Opcode    | Unused  |  Reg S1 |       Unused        |
+-------------+---------+---------+---------------------+
 Word 2:
 31                                         0
+---------------------------------------------+
|             32-bit Memory Address           |
+---------------------------------------------+
```

---

## Implemented Instructions
- **Data Movement**:
  - **L Rd, addr** (`0x02`): Load register Rd from 32-bit absolute address (occupies 2 words).
  - **ST Rs, addr** (`0x03`): Store register Rs to 32-bit absolute address (occupies 2 words).
- **Arithmetic**:
  - **ADD Rd, Rs1, Rs2** (`0x01`): Add Rs1 and Rs2, store result in Rd.
- **Control Flow**:
  - **HALT** (`0x3F`): Halts the machine.

---

## Unimplemented Instructions / Features
- **Register-Indirect & Displacement Addressing**: Indexed addressing and register displacements are not modeled.
- **Branches and Jumps**: Branch instructions and condition registers are omitted.
- **Caches & Pipelining**: Memory and instruction caches are not simulated.

## Architectural Design Purpose

Designed as a research vehicle for high-performance mini-computers and telephone switching nodes, optimizing compiler-targetable instructions.

## Target Purpose Stress Program

```assembly
# IBM 801 Vector Addition
L R0, valA0
L R1, valB0
ADD R0, R0, R1
ST R0, valC0
```
