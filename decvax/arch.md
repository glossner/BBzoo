# DEC VAX Architecture

- **Designer**: C. Gordon Bell & DEC Team
- **Year Introduced**: 1977

## Unique Features
- **Orthogonal CISC Design**: Addressing modes are completely decoupled from opcodes. Any general-purpose register (including PC R15) can be used with any mode.
- **Virtual Address Extension**: The VAX architecture extended the PDP-11 to support a full 32-bit virtual address space, resolving memory capacity limitations.
- **Rich Instruction Set**: Features massive instruction variety including direct hardware support for decimal arithmetic, queue operations, and variable-bit fields.

---

## Instruction Formats

To stay compatible with the word-aligned BrooksZoo simulation environment, VAX instructions are packed into 32-bit words:
```text
 31          24 23          16 15           8 7            0
+--------------+--------------+--------------+--------------+
|    Opcode    |   Src Spec   |   Dst Spec   |    Unused    |
+--------------+--------------+--------------+--------------+
```
Operand specifiers are formatted as a single byte:
```text
 7            4 3            0
+--------------+--------------+
|     Mode     |   Register   |
+--------------+--------------+
```
- **Register mode (5)**: Operand is in GPR `Rn`.
- **Register deferred (6)**: Operand is in memory at address in `Rn`.
- **Autoincrement (8)**: Operand is in memory at address in `Rn`, then `Rn` is incremented.
- **Immediate (8, with PC R15)**: Operand is a 32-bit immediate value following in the next word.

---

## Implemented Instructions
- **MOVL src, dst** (opcode D0 hex): Copies the 32-bit value from `src` to `dst`.
- **ADDL2 src, dst** (opcode C0 hex): Adds the 32-bit `src` to `dst`.
- **SUBL2 src, dst** (opcode C2 hex): Subtracts the 32-bit `src` from `dst`.
- **HALT** (opcode 00 hex): Halts execution.

---

## Unimplemented Instructions / Features
- **3-Operand Instructions**: Operations like `ADDL3` (Add Longword 3-operand) are not supported.
- **Alternative Data Types**: Quadword, octaword, floating-point, and packed decimal data types are not simulated.
- **Non-word alignment**: Variable-length byte-level streams (where opcodes and specifiers cross 32-bit word boundaries) are not simulated.

## Architectural Design Purpose

Virtual memory VAX minicomputer for enterprise computing featuring a comprehensive orthogonal instruction set.

## Target Purpose Stress Program

```assembly
# DEC VAX Polynomial evaluation: y = (x + a) - b
MOVL #20, R0
MOVL #21, R1
MOVL #22, R2
MOVL (R0), R3
ADDL2 (R1), R3
SUBL2 (R2), R3
MOVL #23, R4
MOVL R3, (R4)
HALT
```
