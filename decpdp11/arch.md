# DEC PDP-11 Architecture

- **Designers**: C. Gordon Bell, Harold McFarland, and others (for Digital Equipment Corporation)
- **Year Introduced**: 1970

## Unique Features
- **Extreme Orthogonality**: Every instruction can utilize any register (R0-R7) combined with any of the 8 addressing modes for both source and destination operands.
- **PC as a General Purpose Register**: The Program Counter is represented by register $R7$. Because of this, immediate values and absolute addressing are synthesized naturally by referencing $R7$ in autoincrement or index modes.
- **Stack Pointer as R6**: Register $R6$ acts as the hardware Stack Pointer (SP) for push/pop operations.
- **Memory-Mapped I/O (MMIO)**: Pioneered MMIO, placing peripheral device registers directly into the physical memory address space (no separate I/O instructions).

---

## Instruction Formats

The standard double-operand instruction (MOV, ADD, SUB, etc.) utilizes the following 16-bit format:
```text
 15    12 11     9 8       6 5       3 2       0
+--------+---------+---------+---------+---------+
| Opcode |  S-Mode |  S-Reg  |  D-Mode |  D-Reg  |
+--------+---------+---------+---------+---------+
```
- **Opcode** (bits 12-15): 4-bit instruction code.
- **S-Mode / S-Reg** (bits 6-11): Source operand addressing mode (3 bits) and register (3 bits).
- **D-Mode / D-Reg** (bits 0-5): Destination operand addressing mode (3 bits) and register (3 bits).

---

## Implemented Instructions
- **Double-Operand Instructions**:
  - **MOV** (0x01): Move source operand to destination.
  - **ADD** (0x02): Add source operand to destination ($dst \leftarrow dst + src$).
  - **SUB** (0x03): Subtract source operand from destination ($dst \leftarrow dst - src$).
- **Control Flow / System**:
  - **HALT** (0x0000): Stop execution.

---

## Implemented Addressing Modes
- **Mode 0 (Register)**: Operand is in $Rn$.
- **Mode 1 (Register Deferred / Indirect)**: Address of operand is in $Rn$.
- **Mode 2 (Autoincrement)**: Address of operand is in $Rn$, then $Rn$ is incremented by 1 (or 2 for word operations; in this simplified simulator it increments by 1).

---

## Unimplemented Instructions / Features
- **Unimplemented Addressing Modes**: Autoincrement Deferred (mode 3), Autodecrement (mode 4), Autodecrement Deferred (mode 5), Index (mode 6), and Index Deferred (mode 7) are not implemented.
- **Byte-level Operations**: Byte/Word selection flag (bit 15 of opcode in standard PDP-11) is not implemented.
- **Single-Operand Instructions**: Unary operations like `CLR` (Clear), `DEC` (Decrement), `INC` (Increment), `TST` (Test), and `NEG` (Negate) are not implemented.
- **Conditionals and Traps**: Conditional branches (`BEQ`, `BNE`, etc.), subroutines (`JSR`/`RTS`), and software trap instructions (`TRAP`, `EMT`) are not implemented.

## Architectural Design Purpose

Highly versatile general-purpose minicomputer and time-sharing system introducing a clean, orthogonal addressing model.

## Target Purpose Stress Program

```assembly
# DEC PDP-11 Polynomial evaluation: y = (x + a) - b
MOV #20, R0    # Address of x
MOV #21, R1    # Address of a
MOV #22, R2    # Address of b
MOV (R0), R3   # Load x
ADD (R1), R3   # Add a
SUB (R2), R3   # Subtract b
MOV #23, R4    # Address of y
MOV R3, (R4)   # Store y
HALT
```
