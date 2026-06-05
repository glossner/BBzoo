# PowerPC Architecture

- **Designers**: Apple, IBM, and Motorola (AIM Alliance)
- **Year Introduced**: 1991

## Unique Features
- **AIM Alliance Collaboration**: Developed as a joint effort between Apple, IBM, and Motorola to create a standard RISC microprocessor architecture.
- **32-bit RISC**: Employs a 32-bit data path, 32-bit memory address space, and 32 general-purpose 32-bit registers ($R0$ through $R31$).
- **Strict Load/Store Paradigm**: Separates memory access from data operations, ensuring arithmetic only operates on registers.

---

## Instruction Formats

PowerPC instructions are 32-bit words, with absolute address parameters occupying a second word:

### 1-Word Instructions (Register-to-Register and Halts)
```text
 31         26 25     21 20     16 15     11 10        0
+-------------+---------+---------+---------+-----------+
|   Opcode    |  Reg RD |  Reg RS1|  Reg RS2|  Unused   |
+-------------+---------+---------+---------+-----------+
```

### 2-Word Instructions (Load and Store absolute)
```text
 Word 1:
 31         26 25     21 20     16 15                 0
+-------------+---------+---------+---------------------+
|   Opcode    | Unused  |  Reg RS1|       Unused        |
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
  - **LWZ Rd, addr** (`0x02`): Load register Rd (encoded in RS1 field) from 32-bit absolute address (occupies 2 words).
  - **STW Rs, addr** (`0x03`): Store register Rs (encoded in RS1 field) to 32-bit absolute address (occupies 2 words).
- **Arithmetic**:
  - **ADD Rd, Rs1, Rs2** (`0x01`): Add Rs1 and Rs2, store result in Rd.
- **Control Flow**:
  - **HALT** (`0x3F`): Halts the machine.

---

## Unimplemented Instructions / Features
- **Indexed & Displacement Addressing**: PowerPC's standard base+offset and indexed load/store instructions are not modeled.
- **Floating-Point Operations**: Single and double precision floating-point registers and instructions are omitted.
- **Branches and Condition Registers**: Conditional branches and Link/Count registers are not simulated.
- **Caches & Pipelining**: Memory and instruction caches are not simulated.

## Architectural Design Purpose

Designed as a scalable RISC architecture intended for high-performance personal computers, workstations, and embedded system applications.

## Target Purpose Stress Program

```assembly
# PowerPC Vector Addition
LWZ R0, valA0
LWZ R1, valB0
ADD R0, R0, R1
STW R0, valC0
```
