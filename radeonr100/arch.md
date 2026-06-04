# ATI Radeon R100 Architecture

- **Designer**: ATI Technologies Team
- **Year Introduced**: 2000

## Unique Features
- **Charisma Engine**: Hardware Transform and Lighting (T&L) implementation capable of processing 30 million polygons/sec.
- **Pixel Tapestry**: A 3D rasterization engine that allowed up to 3 texture coordinate systems/texels to be combined per pixel rendering pass.
- **HyperZ**: Bandwidth-saving technology featuring Hierarchical Z, Z-compression, and Fast Z-clear.

---

## Instruction Formats

In our simulation, instructions are 32-bit wide. Absolute load/store instructions are 2 words (the instruction word followed by a 32-bit absolute address). Computational instructions occupy 1 word.

### 1-Word Instruction (TAPESTRY / HALT)
```text
 31      24 23    22 21    20 19    18 17    16 15    14 13    12 11     0
+---------+---------+---------+---------+---------+---------+---------+-----+
| Opcode  |  Dest   | Unused  |  SrcA   |  SrcB   |  SrcC   | Unused  |
+---------+---------+---------+---------+---------+---------+---------+-----+
```

### 2-Word Instruction (LD / ST)
```text
 Word 1:
 31      24 23    22 21                                                   0
+---------+---------+-----------------------------------------------------+
| Opcode  | Reg/Src |                       Unused                        |
+---------+---------+-----------------------------------------------------+
 Word 2:
 31                                                                       0
+-------------------------------------------------------------------------+
|                          32-bit Target Address                          |
+-------------------------------------------------------------------------+
```

---

## Implemented Instructions
- **LD R[dest], addr** (`0x10`): Load 32-bit value from memory address `addr` into GPR R[dest].
- **ST R[src], addr** (`0x30`): Store 32-bit value from GPR R[src] into memory address `addr`.
- **TAPESTRY R[dest], R[srcA], R[srcB], R[srcC]** (`0x20`): Performs Pixel Tapestry combining logic: `R[dest] = (R[srcA] * R[srcB]) + R[srcC]`.
- **HALT** (`0x00`): Halts execution.
