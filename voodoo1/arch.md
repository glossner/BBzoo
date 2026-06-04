# 3dfx Voodoo1 Architecture

- **Designer**: 3dfx Interactive Team
- **Year Introduced**: 1996

## Unique Features
- **Pioneer of Consumer 3D**: Voodoo1 was the defining add-in card for early 3D gaming, specializing in hardware rasterization, texture mapping, and bilinear filtering.
- **Dedicated Rasterizer and Texture Mapping Units (TMUs)**: Separated texturing and pixel processing workloads.
- **Bilinear Filtering and Depth Buffering**: Provided hardware-accelerated perspective-correct texture mapping and Z-buffering.

---

## Instruction Formats

In our simulation, instructions are 32-bit wide. Absolute load/store instructions are 2 words (the instruction word followed by a 32-bit absolute address). Computational instructions occupy 1 word.

### 1-Word Instruction (ADD / HALT)
```text
 31      24 23    20 19    16 15    12 11        0
+---------+---------+---------+---------+---------+
| Opcode  |  Dest   |  Src1   |  Src2   | Unused  |
+---------+---------+---------+---------+---------+
```

### 2-Word Instruction (LD / ST)
```text
 Word 1:
 31      24 23    20 19                          0
+---------+---------+-----------------------------+
| Opcode  | Reg/Src |           Unused            |
+---------+---------+-----------------------------+
 Word 2:
 31                                              0
+-------------------------------------------------+
|              32-bit Target Address              |
+-------------------------------------------------+
```

---

## Implemented Instructions
- **LD R[dest], addr** (`0x10`): Load 32-bit value from memory address `addr` into GPR R[dest].
- **ST R[src], addr** (`0x30`): Store 32-bit value from GPR R[src] into memory address `addr`.
- **ADD R[dest], R[src1], R[src2]** (`0x20`): Add values in R[src1] and R[src2], store result in R[dest].
- **HALT** (`0x00`): Halts execution.
