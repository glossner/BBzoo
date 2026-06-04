# ARM Mali-200 Architecture

- **Designer**: Falanx Microsystems / ARM Team
- **Year Introduced**: 2007

## Unique Features
- **First Programmable Mali GPU**: Mali-200 introduced programmable shader units to the Mali family, implementing the OpenGL ES 2.0 API.
- **Tile-Based Rendering**: Employs tile-based rendering to maximize energy efficiency and minimize memory traffic on mobile SoCs.
- **SIMD Vector Execution**: Utilizes 4-element SIMD vector registers to process RGBA color and XYZW vertex coordinate formats in parallel.

---

## Instruction Formats

In our simulation, instructions are 32-bit wide. Absolute load/store instructions are 2 words (the instruction word followed by a 32-bit absolute address). Vector computational instructions occupy 1 word.

### 1-Word Instruction (VADD / HALT)
```text
 31      24 23    22 21    20 19    18 17    16 15    14 13              0
+---------+---------+---------+---------+---------+---------+-------------+
| Opcode  | Dest_V  | Unused  | Src1_V  | Src2_V  |           Unused    |
+---------+---------+---------+---------+---------+---------+-------------+
```

### 2-Word Instruction (VLD / VST)
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
- **VLD V[dest], addr** (`0x10`): Load 4 consecutive 32-bit values from memory starting at address `addr` into SIMD vector register V[dest].
- **VST V[src], addr** (`0x30`): Store 4 consecutive 32-bit values from SIMD vector register V[src] into memory starting at address `addr`.
- **VADD V[dest], V[src1], V[src2]** (`0x20`): Perform element-wise parallel vector addition: `V[dest][j] = V[src1][j] + V[src2][j]` for `j` in 0..3.
- **HALT** (`0x00`): Halts execution.
