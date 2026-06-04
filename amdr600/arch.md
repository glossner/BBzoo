# AMD R600 Architecture

- **Designer**: AMD Team
- **Year Introduced**: 2007

## Unique Features
- **First Unified Shader GPU from AMD**: Moved away from separate pixel and vertex pipelines to a unified shader array.
- **VLIW5 (Very Long Instruction Word) Engine**: Executes up to 5 scalar instructions in parallel in a single VLIW instruction packet.
- **Ring Bus Memory Controller**: A 512-bit ring bus structure for optimizing memory bandwidth.

---

## Instruction Formats

In our simulation, instructions are 32-bit wide VLIW packets containing an ALU slot (Slot A, bits 16-31) and a Memory slot (Slot B, bits 0-15).

```text
 31  28 27  24 23  20 19  16 15  12 11   8 7            0
+------+------+------+------+------+------+--------------+
| opA  |destA |src1A |src2A | opB  | regB |  addrOffset  |
+------+------+------+------+------+------+--------------+
```

### Slot A (ALU)
- **opA** (bits 28-31): `0` = NOP, `1` = ADD, `2` = HALT.
- **destA** (bits 24-27): Destination GPR (R0-R3).
- **src1A** (bits 20-23): Source GPR 1 (R0-R3).
- **src2A** (bits 16-19): Source GPR 2 (R0-R3).

### Slot B (Memory)
- **opB** (bits 12-15): `0` = NOP, `1` = LD, `2` = ST.
- **regB** (bits 8-11): GPR (R0-R3) to load/store.
- **addrOffset** (bits 0-7): 8-bit memory address.

---

## Implemented Instructions
- **VLIW Instruction Packet**:
  - `ADD R[destA], R[src1A], R[src2A] | LD R[regB], addrOffset`
  - `HALT | NOP`
  - etc.

## Architectural Design Purpose

Unified shader processing running VLIW instructions for parallel vertex, pixel, and physics computation.

## Target Purpose Stress Program

```assembly
# AMD R600 VLIW Arithmetic: R0 = R0 + R1
NOP | LD R0, 20
NOP | LD R1, 21
ADD R0, R0, R1 | NOP
NOP | ST R0, 22
```
