# PowerVR Series 1 Architecture

- **Designer**: Imagination Technologies / VideoLogic Team
- **Year Introduced**: 1996

## Unique Features
- **Tile-Based Deferred Rendering (TBDR)**: PowerVR splits the screen into small tiles (e.g., 32x32) and buffers all geometry for a frame before rendering.
- **Hidden Surface Removal (HSR) in Hardware**: Compares depth of all overlapping polygons in a tile, discarding obscured pixels so that only visible pixels are shaded, completely eliminating overdraw.
- **Low Memory Bandwidth**: Performs depth testing on-chip inside the tile buffer, eliminating the need for external Z-buffer reads and writes.

---

## Instruction Formats

In our simulation, instructions are 32-bit wide. Absolute load/store instructions are 2 words (the instruction word followed by a 32-bit absolute address). Computational instructions occupy 1 word.

### 1-Word Instruction (HSR / HALT)
```text
 31      24 23    22 21    20 19    18 17    16 15    14 13    12 11     0
+---------+---------+---------+---------+---------+---------+---------+-----+
| Opcode  |  Dest   | Unused  |  Src1   |  Src2   |depth_reg| Unused  |
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
- **HSR R[dest], R[src1], R[src2], R[depth_reg]** (`0x20`): Conditional addition representing Hidden Surface Removal: if `R[depth_reg]` is less than the internal `DEPTH` register, set `R[dest] = R[src1] + R[src2]` and update `DEPTH = R[depth_reg]`.
- **HALT** (`0x00`): Halts execution.

## Architectural Design Purpose

Tile-Based Deferred Rendering with hardware Hidden Surface Removal (HSR) depth sorting.

## Target Purpose Stress Program

```assembly
# PowerVR Series 1 Fragment HSR: colorC = HSR(colorA, colorB, Depth)
LD R3, 24    # Fragment Depth
LD R0, 20    # Color A
LD R1, 21    # Color B
HSR R0, R0, R1, R3
ST R0, 22
```
