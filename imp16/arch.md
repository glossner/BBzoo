# National Semiconductor IMP-16 Architecture

- **Designers**: National Semiconductor
- **Year Introduced**: 1973

## Unique Features
- **Early Bit-Slice System**: One of the earliest commercially available multi-chip bit-slice microprocessors. It is constructed from four 4-bit PMOS RALU (Register and ALU) slices.
- **Microprogrammed Control**: Uses one or more CROM (Control and ROM) chips to decode instructions and sequence micro-operations.
- **4 General Accumulators**: Features 4 accumulators (AC0, AC1, AC2, AC3) that act as target locations for calculations.

---

## Instruction Formats

The custom 16-bit macro-instruction set for this simulated 16-bit IMP-16-based core uses the following formats:

### Register-Register Instructions (ADD, SUB, AND, OR)
```text
 15      12 11     8 7      4 3      0
+----------+--------+--------+--------+
|  Opcode  |   Rd   |   Ra   |   Rb   |
+----------+--------+--------+--------+
```

### Memory & Jump Instructions (LD, ST, JNZ, JMP)
```text
Word 1:
 15      12 11     8 7               0
+----------+--------+-----------------+
|  Opcode  |  Rd/Rs |    Reserved     |
+----------+--------+-----------------+
Word 2:
 15                                  0
+-------------------------------------+
|         16-bit Memory Address       |
+-------------------------------------+
```

---

## Implemented Instructions
- **HALT** (0000): Halt CPU execution.
- **LD** (0001): Load 16-bit value from memory address to Rd.
- **ST** (0010): Store 16-bit value from Rs to memory address.
- **ADD** (0011): Add registers: `Rd = Ra + Rb`.
- **SUB** (0100): Subtract registers: `Rd = Ra - Rb`.
- **AND** (0101): Logical AND: `Rd = Ra & Rb`.
- **OR** (0110): Logical OR: `Rd = Ra | Rb`.
- **JNZ** (0111): Jump to address if Rd is not zero.
- **JMP** (1000): Unconditional jump to address.
- **LDI** (1001): Load indirect: `Rd = mem[Rs]`.
- **STI** (1010): Store indirect: `mem[Rd] = Rs`.

---

## Unimplemented Instructions / Features
- **Extended CROM Instructions**: Double-precision math and search instructions that could be added with secondary CROMs are not modeled.

## Architectural Design Purpose
Early industrial control systems, minicomputer replacement, and instrumentation.
