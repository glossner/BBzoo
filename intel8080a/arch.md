# Intel 8080A Architecture

- **Designers**: Federico Faggin and Masatoshi Shima
- **Year Introduced**: 1974

## Unique Features
- **8-bit Accumulator & Register Pair Architecture**: In addition to an 8-bit accumulator ($A$), it features 6 other 8-bit general registers ($B$, $C$, $D$, $E$, $H$, $L$). The registers can be paired (e.g. $HL$) to form 16-bit pointers to address memory.
- **16-bit Address Space**: Allows addressing up to 64 KB of memory space natively using little-endian byte ordering (low-order byte first, followed by high-order byte).
- **Substantial Operating System Impact**: Serving as the target platform for CP/M (Control Program/Monitor), it helped pioneer the standard software model for personal computers.

---

## Instruction Formats

Intel 8080A instructions are 1, 2, or 3 bytes long depending on operand types:

### 1-Byte Instructions (Register and Implicit operations)
```text
 7            0
+--------------+
|    Opcode    |
+--------------+
```

### 3-Byte Instructions (Direct Memory Addressing)
```text
 7            0 7            0 7            0
+--------------+--------------+--------------+
|    Opcode    | Address Low  | Address High |
+--------------+--------------+--------------+
```

---

## Implemented Instructions
- **Data Movement**:
  - **LDA addr** (`0x3A`): Load Accumulator directly from absolute 16-bit memory address.
  - **STA addr** (`0x32`): Store Accumulator directly to absolute 16-bit memory address.
  - **MOV B, A** (`0x47`): Move Accumulator A contents to register B.
- **Arithmetic**:
  - **ADD B** (`0x80`): Add register B to Accumulator A (result in A, updates carry flag).
- **Control Flow**:
  - **HLT** (`0x76`): Halts execution.

---

## Unimplemented Instructions / Features
- **General Purpose Registers (C, D, E, H, L)**: Not simulated except for Register B which is implemented for vector addition.
- **Register Pair Pointers (BC, DE, HL, SP)**: Pointer-indirect operations and stack-based memory accesses (PUSH, POP, LXI) are unimplemented.
- **Arithmetic/Logic Variations**: Subtraction, logical operations (AND, OR, XOR), rotates, increments, and decrements are not supported.
- **Branch and Subroutine Flow**: Absolute jump (`JMP`), conditional jumps (`JZ`, `JNZ`, `JC`, `JNC`), calls (`CALL`), and returns (`RET`) are not simulated.
