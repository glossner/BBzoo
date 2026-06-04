# NVIDIA GeForce 256 Architecture

- **Designer**: NVIDIA Team
- **Year Introduced**: 1999

## Unique Features
- **The World's First "GPU"**: Introduced hardware Transform and Lighting (T&L), offloading vertex processing from the CPU.
- **Register Combiners**: Replaced the fixed-function texture blending stage with a set of configurable combiners capable of performing vector multiplications and additions in a single pass.
- **Unified 4-Pixel Pipeline**: Four rasterization pipelines operating in parallel.

---

## Instruction Formats

In our simulation, instructions are 32-bit wide. Absolute load/store instructions are 2 words (the instruction word followed by a 32-bit absolute address). Computational instructions occupy 1 word.

### 1-Word Instruction (COMBINE / HALT)
```text
 31      24 23    22 21    20 19    18 17    16 15    14 13    12 11     0
+---------+---------+---------+---------+---------+---------+---------+-----+
| Opcode  |  Dest   | Unused  |  SrcA   |  SrcB   |  SrcC   |  SrcD   |Unused
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
- **COMBINE R[dest], R[srcA], R[srcB], R[srcC], R[srcD]** (`0x20`): Performs the register combiner operation: `R[dest] = (R[srcA] * R[srcB]) + (R[srcC] * R[srcD])`.
- **HALT** (`0x00`): Halts execution.
