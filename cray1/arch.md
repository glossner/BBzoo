# Cray-1 Architecture

- **Designer**: Seymour Cray (for Cray Research)
- **Year Introduced**: 1976

## Unique Features
- **Vector Registers**: Introduced vector registers ($V0$-$V7$) capable of holding up to 64 elements of 64-bit data.
- **Vector Length (VL) Register**: A specialized control register that sets the count of vector elements processed by subsequent vector operations (from 1 to 64).
- **Differentiated Register Files**: Features address registers ($A0$-$A7$, 24-bit) for memory references and scalar registers ($S0$-$S7$, 64-bit) for general computations, separated from the vector registers.
- **Pipelined Vector Operations**: Allows operations to execute element-by-element through specialized functional units.

---

## Instruction Formats

Cray-1 instructions are 16-bit or 32-bit (divided into 16-bit parcels). The 16-bit instruction parcel uses the following format:
```text
 15      12 11     9 8       6 5       3 2       0
+----------+--------+---------+---------+---------+
|    g     |   h    |    i    |    j    |    k    |
+----------+--------+---------+---------+---------+
```
- **g** (bits 12-15): Primary opcode (4 bits).
- **h** (bits 9-11): Sub-opcode (3 bits).
- **i** (bits 6-8): Destination register select (3 bits).
- **j** (bits 3-5): First source register select (3 bits).
- **k** (bits 0-2): Second source register select (3 bits).

For Load Immediate instructions, the immediate value is placed in the lower 12 bits:
```text
 15      12 11                               0
+----------+---------------------------------+
|    g     |         Immediate Value         |
+----------+---------------------------------+
```

---

## Implemented Instructions
- **Vector Length Control**:
  - **VL := A(j)**: Set vector length register to the value in address register $A_j$.
- **Address Registers**:
  - **A(i) := mem[A(j)]**: Load address register from memory.
  - **mem[A(j)] := A(i)**: Store address register to memory.
  - **A(i) := A(j) + A(k)**: Address register addition.
  - **A(i) := Immediate**: Load 12-bit immediate into address register.
- **Scalar Registers**:
  - **S(i) := S(j) + S(k)**: Scalar register addition.
- **Vector Registers**:
  - **V(i) := mem[A(j)]**: Load vector register from memory.
  - **mem[A(j)] := V(i)**: Store vector register to memory.
  - **V(i) := V(j) + V(k)**: Element-wise vector addition.
  - **V(i) := V(j) + S(k)**: Element-wise scalar-vector addition.
- **Control Flow**:
  - **HLT**: Halt simulation.

---

## Unimplemented Instructions / Features
- **Vector Chaining**: Overlapping execution where a vector unit starts consuming elements from a producing unit before it completes is not simulated.
- **Backup Registers (B & T)**: The 64-word intermediate registers ($B0$-$B77$ and $T0$-$T77$) are not implemented.
- **Floating-Point Arithmetic**: Single-precision, double-precision, reciprocal approximation, and population count functional units are not implemented.
- **Logical and Shift Operations**: Bitwise AND, OR, XOR, and vector shifts are not implemented.
