# Intel 3002 Architecture

- **Designers**: Intel
- **Year Introduced**: 1974

## Unique Features
- **2-bit Bit-Slice ALU**: The Intel 3002 CPE (Central Processing Element) is one of the earliest bit-slice architectures, operating as a 2-bit wide slice.
- **Schottky Bipolar Technology**: Offered high speed for its time, allowing it to compete with high-speed minicomputer ALUs.
- **Dedicated Accumulator (AC) and Temporary Register (T)**: Features a specific accumulator (AC) register and a scratchpad memory of 11 registers (R0-R9, plus RT/T).

---

## Instruction Formats

The custom 16-bit macro-instruction set for this simulated 16-bit Intel 3002-based core uses the following formats:

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
- **Microprogrammed Sequencer**: Hardwired macro-instruction decode and FSM sequencing replace the microcode ROM and Intel 3001 Microprogram Control Unit (MCU).

## Architectural Design Purpose
High-speed CPU design for minicomputers, controllers, and custom processing elements.
