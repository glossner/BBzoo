# IBM System/360 Architecture

- **Designers**: Gene Amdahl, Fred Brooks, and Gerrit Blaauw (for IBM)
- **Year Introduced**: 1964

## Unique Features
- **Byte Addressability & 32-bit Words**: Introduced the standard 8-bit byte, 32-bit word size, and byte-addressable memory organization to the industry.
- **Variable Instruction Lengths**: Instructions can be 2 bytes (16-bit), 4 bytes (32-bit), or 6 bytes (48-bit). Opcode prefix bits (top 2 bits) specify the instruction length.
- **Base-Displacement Addressing**: Memory references do not use direct absolute addresses. Instead, they calculate target addresses dynamically using a base register, index register, and a 12-bit unsigned offset.
- **Unified Families**: First architecture designed to span a wide range of performance and cost points (from small microcoded systems to high-speed mainframe supercomputers) running the same binary software.

---

## Instruction Formats

### RR Format (Register-Register, 16-bit)
```text
 0        7   8  11  12 15
+-----------+------+------+
|  Opcode   |  R1  |  R2  |
+-----------+------+------+
```

### RX Format (Register-Indexed-Storage, 32-bit)
```text
 0        7   8  11  12 15  16 19  20         31
+-----------+------+------+------+---------------+
|  Opcode   |  R1  |  X2  |  B2  |       D2      |
+-----------+------+------+------+---------------+
```
- **R1**: Target/source register.
- **X2**: Index register.
- **B2**: Base register.
- **D2**: 12-bit unsigned displacement.
- Address calculation: $\text{Effective Address} = \text{Reg}[B2] + \text{Reg}[X2] + D2$.

---

## Implemented Instructions
- **RR Instructions**:
  - **LR** (0x18): Load Register ($R1 \leftarrow R2$).
  - **AR** (0x1A): Add Register ($R1 \leftarrow R1 + R2$).
  - **SR** (0x1B): Subtract Register ($R1 \leftarrow R1 - R2$).
  - **CR** (0x19): Compare Register (subtracts $R2$ from $R1$ to set status flags).
- **RX Instructions**:
  - **L** (0x58): Load from memory into register.
  - **A** (0x5A): Add memory to register.
  - **S** (0x5B): Subtract memory from register.
  - **ST** (0x50): Store register to memory.
  - **BC** (0x47): Branch on Condition.

---

## Unimplemented Instructions / Features
- **RS, SI, and SS formats**: Instruction formats for register-to-storage, storage-immediate, and storage-to-storage memory blocks are not implemented.
- **Decimal Instruction Set**: Packed decimal arithmetic instructions are not implemented.
- **Floating Point**: Single-precision, double-precision, and extended-precision floating point instructions are not implemented.
- **Privileged System State**: Program Status Word (PSW) controls, supervisor calls, and storage protection keys are not implemented.
- **Channel I/O**: Mainframe input/output channels are not modeled.

## Architectural Design Purpose

A unified, single ISA mainframe family consolidating business and scientific calculations under one architecture.

## Target Purpose Stress Program

```assembly
# IBM System/360 Polynomial evaluation: y = (x + a) - b
L 1, 20    # Load x into register 1
A 1, 21    # Add a
S 1, 22    # Subtract b
ST 1, 23   # Store y
```
