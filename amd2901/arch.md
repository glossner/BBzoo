# AMD Am2901 Architecture

- **Designers**: Advanced Micro Devices (AMD)
- **Year Introduced**: 1975

## Unique Features
- **4-bit Bit-Slice ALU**: The Am2901 is the most iconic member of the Am2900 bit-slice family. It is a 4-bit wide slice containing a 16-word dual-port RAM register file and a high-speed ALU.
- **Cascadable Word Width**: Multiple slices are cascaded in parallel (e.g. four slices for a 16-bit word size) to support arbitrary data and address widths.
- **Dual-port Register File**: Register file ports A and B can be read simultaneously to serve as operands for the ALU.

---

## Instruction Formats

The custom 16-bit macro-instruction set for this simulated 16-bit Am2901-based core uses the following formats:

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

---

## Unimplemented Instructions / Features
- **Microprogrammed Sequencer**: The internal FSM acts as a hardwired macro-instruction decoder rather than utilizing a discrete external Am2909/Am2911 microprogram sequencer.
- **Carry Lookahead**: Ripple carry is assumed for cascading slices.

## Architectural Design Purpose
High-performance, custom-word-width microprogrammable controller used in minicomputers, graphics terminals, and disk controllers.
