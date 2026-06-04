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

## Architectural Design Purpose

Introducing hardware Transform & Lighting (T&L) and configurable Register Combiners for multi-texture pixel blending.

## Target Purpose Stress Program

```assembly
# NVIDIA GeForce 256 texture blend: R0 = (R0 * R1) + (R2 * R3)
LD R1, 24    # Scale factor 1
LD R3, 24    # Scale factor 1
LD R0, 20    # Input color A
LD R2, 21    # Input color B
COMBINE R0, R0, R1, R2, R3
ST R0, 22
```
