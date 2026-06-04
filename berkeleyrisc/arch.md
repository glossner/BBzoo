# Berkeley RISC-I Architecture

- **Designers**: David Patterson and Carlo H. Séquin
- **Year Introduced**: 1981

## Unique Features
- **Register Windows**: Berkeley RISC-I pioneered the register window concept, allowing fast procedure calls by overlapping register sets for parameters, local variables, and return values.
- **Load-Store Design**: Like other RISC processors, it uses explicit load (LD) and store (ST) instructions for memory access, restricting arithmetic operations to registers.
- **Fixed-length 32-bit instructions**: Simplifies instruction decoding compared to contemporary CISC designs.

---

## Instruction Formats

In our simulation, absolute load/store instructions span 2 words where the target address follows the instruction word, while register computational instructions occupy 1 word.

### 1-Word Instructions (ADD)
```text
 31      24 23   19 18   14 13                     5 4      0
+----------+-------+-------+------------------------+-------+
|  Opcode  |  rd   |  rs   |         Unused         |  rm   |
+----------+-------+-------+------------------------+-------+
```

### 2-Word Instructions (LD / ST absolute)
```text
 Word 1:
 31      24 23   19 18                                      0
+----------+-------+----------------------------------------+
|  Opcode  |  rd   |                 Unused                 |
+----------+-------+----------------------------------------+
 Word 2:
 31                                                         0
+-----------------------------------------------------------+
|                   32-bit Target Address                   |
+-----------------------------------------------------------+
```

---

## Implemented Instructions
- **Data Movement**:
  - **LD rd, addr** (`0x01`): Load Word from 32-bit absolute address into register rd.
  - **ST rd, addr** (`0x02`): Store Word from register rd into 32-bit absolute address.
- **Arithmetic**:
  - **ADD rd, rs, rm** (`0x03`): Add register rs and rm and store result in rd.
- **Control Flow**:
  - **HALT** (`0x00`): Halts the machine.

---

## Unimplemented Instructions / Features
- **Register Windows**: Overlapping register windows are not simulated; instead, a flat file of 32 registers is modeled.
- **Branches and Jumps**: Conditional branches (JMP, JMPR) and subroutine calls (CALL, RET) are not simulated.
- **Immediate Addressing**: Immediate values in ALU instructions are not supported.
- **Byte/Halfword Loads and Stores**: Only word-aligned 32-bit accesses are implemented.

## Architectural Design Purpose

Exploring register window designs and simplified load-store RISC instruction layouts to optimize compiler compilation.

## Target Purpose Stress Program

```assembly
# Berkeley RISC-I arithmetic: y = x + a
LD R1, 20
LD R2, 21
ADD R1, R1, R2
ST R1, 22
```
