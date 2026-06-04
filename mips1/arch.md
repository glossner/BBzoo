# MIPS I (R2000) Architecture

- **Designers**: John L. Hennessy and MIPS Computer Systems
- **Year Introduced**: 1986

## Unique Features
- **Load-Store RISC Architecture**: Computational instructions only operate on registers (R0-R31), and memory accesses are limited to explicit Load (LW) and Store (SW) instructions.
- **Register R0 Hardwired to 0**: Register R0 always returns 0 when read, and writes to it are ignored. This simplifies many operations (like register clearing or branch-on-zero).
- **Uniform 32-bit Instruction Encoding**: Every instruction occupies a single 32-bit word, classified into R-type (Register-to-Register), I-type (Immediate/Transfer), or J-type (Jump) formats.

---

## Instruction Formats

In our simulation, absolute load/store instructions span 2 words where the target address follows the instruction word, while register computational instructions occupy 1 word.

### 1-Word Instructions (R-type: ADDU)
```text
 31      26 25    21 20    16 15    11 10     6 5      0
+----------+--------+--------+--------+--------+--------+
|  Opcode  |   rs   |   rt   |   rd   |  shamt |  funct |
+----------+--------+--------+--------+--------+--------+
```

### 2-Word Instructions (LW / SW absolute)
```text
 Word 1:
 31      26 25    21 20    16 15                        0
+----------+--------+--------+--------------------------+
|  Opcode  |   rs   |   rt   |          Unused          |
+----------+--------+--------+--------------------------+
 Word 2:
 31                                                     0
+-------------------------------------------------------+
|                 32-bit Target Address                 |
+-------------------------------------------------------+
```

---

## Implemented Instructions
- **Data Movement**:
  - **LW rt, addr** (`0x23`): Load Word from 32-bit absolute address into register rt.
  - **SW rt, addr** (`0x2B`): Store Word from register rt into 32-bit absolute address.
- **Arithmetic**:
  - **ADDU rd, rs, rt** (opcode `0x00`, funct `0x21`): Add register rs and rt (unsigned) and store result in rd.
- **Control Flow**:
  - **HALT** (`0x3F`): Halts the machine.

---

## Unimplemented Instructions / Features
- **Branch and Jump Instructions**: Conditional branches (BEQ, BNE) and jumps (J, JAL, JR) are not simulated.
- **Signed Arithmetic & Exceptions**: Add signed (ADD) and subtraction (SUB, SUBU) instructions, and hardware exception triggers on overflow, are unimplemented.
- **Shift and Logical Operations**: Logical (AND, OR, XOR, NOR) and shift (SLL, SRL, SRA) instructions are not supported.
- **Coprocessors & Floating Point Unit (FPU)**: CP0 system registers, Virtual Memory MMU, and floating-point registers/instructions are not modeled.

## Architectural Design Purpose

High-performance workstation and enterprise database query acceleration using a clean, pipelined load-store RISC ISA.

## Target Purpose Stress Program

```assembly
# MIPS I arithmetic: y = x + a
LW R1, 20
LW R2, 21
ADDU R1, R1, R2
SW R1, 22
HALT
```
