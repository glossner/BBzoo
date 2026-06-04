# DEC PDP-8 Architecture

- **Designers**: C. Gordon Bell and Edson de Castro (for Digital Equipment Corporation)
- **Year Introduced**: 1965

## Unique Features
- **12-bit Word Width**: The PDP-8 is one of the most famous 12-bit minicomputers, designed to be extremely minimalistic and cost-effective.
- **Operate Instruction Micro-Programming**: The `OPR` instruction uses individual bits of the instruction word to trigger micro-operations sequentially within a single instruction cycle (e.g., clearing the accumulator, complementing it, and incrementing it).
- **Page-Relative Addressing**: Memory is divided into 128-word pages. Standard memory instructions reference page 0 or the current page with a 7-bit offset.

---

## Instruction Formats

Standard memory-reference instructions (AND, TAD, DCA, JMP, etc.) use the following format:
```text
 0          2   3   4   5                 11
+-------------+---+---+---------------------+
|   Opcode    | I | Z |     Offset          |
+-------------+---+---+---------------------+
```
- **Opcode** (bits 0-2): Operation code.
- **I** (bit 3): Indirect addressing flag.
- **Z** (bit 4): Page selector (0 = Page 0, 1 = Current Page).
- **Offset** (bits 5-11): 7-bit page relative offset.

Operate (`OPR`) instructions use the following format:
```text
 0          2   3                             11
+-------------+-------------------------------+
|     111     |       Micro-operations        |
+-------------+-------------------------------+
```

---

## Implemented Instructions
- **AND** (000): Logical AND memory with Accumulator.
- **TAD** (001): Two's complement addition of memory to Accumulator (modifies Link bit on overflow).
- **ISZ** (010): Increment memory and skip next instruction if memory becomes zero.
- **DCA** (011): Deposit Accumulator to memory and clear Accumulator.
- **JMS** (100): Jump to subroutine.
- **JMP** (101): Jump to address.
- **OPR Group 1**: CLA (Clear ACC), CLL (Clear Link), CMA (Complement ACC), CML (Complement Link), RAR (Rotate ACC Right), RAL (Rotate ACC Left), IAC (Increment ACC).
- **OPR Group 2**: SMA (Skip on Minus ACC), SZA (Skip on Zero ACC), SNL (Skip on Non-zero Link), REV (Reverse skip conditions), CLA (Clear ACC).

---

## Unimplemented Instructions / Features
- **IOT** (110): Input/Output Transfer (opcode reserved but not wired to external hardware peripherals).
- **Auto-indexing registers**: Auto-increment of memory addresses at locations 0010-0017 octal when accessed indirectly is not implemented.
- **Extended Memory Control**: Multi-field memory addressing (via extended data field registers) is not implemented (limited to the base 4KB address space).

## Architectural Design Purpose

Low-cost minicomputer designed for laboratory automation, process control, and industrial interfacing.

## Target Purpose Stress Program

```assembly
# DEC PDP-8 Loop Process: y = x + a
CLA CLL
TAD 20    # Load x
TAD 21    # Add a
DCA 22    # Store to y
HLT
```
