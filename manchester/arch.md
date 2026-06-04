# Manchester Baby Architecture

- **Designers**: Frederic C. Williams, Tom Kilburn, and Geoff Tootill
- **Year Introduced**: 1948

## Unique Features
- **First Stored-Program Computer**: Completed in 1948 at the University of Manchester, it was the first machine in history to store both instructions and data in a single read-write electronic memory.
- **Williams Tube Memory**: Used Cathode Ray Tube (CRT) electrostatic storage tubes (Williams tubes) to hold 32 words of 32 bits each.
- **Subtract-Only Arithmetic**: Had no hardware addition unit in the ALU. To perform addition $A + B$, the program must synthesize it using subtraction: $A + B = -(-B - A)$.
- **Single Accumulator**: Operates with a single 32-bit Accumulator register ($A$) and a Program Counter (called Control Instruction, $CI$).

---

## Instruction Formats

Manchester Baby instructions are 32 bits wide, but utilize only the lower 16 bits. The layout is:
```text
 31                         16 15      13 12                 0
+-----------------------------+----------+--------------------+
|          Reserved           |  Opcode  |      Address       |
+-----------------------------+----------+--------------------+
```
- **Opcode** (bits 13-15): 3-bit operation code.
- **Address** (bits 0-12): 13-bit memory address.

---

## Implemented Instructions
- **JMP addr** (0): PC is loaded with value from memory address `addr`.
- **JPR addr** (1): PC is incremented by value from memory address `addr` (relative jump).
- **LDN addr** (2): Load negative ($A \leftarrow -\text{mem}[addr]$).
- **STO addr** (3): Store value of Accumulator $A$ to memory address `addr`.
- **SUB addr** (4): Subtract memory value from Accumulator ($A \leftarrow A - \text{mem}[addr]$).
- **STP** (7): Stop / Halt.

---

## Unimplemented Instructions / Features
- **Williams Tube Refresh Cycles**: The electrostatic charge decay and refresh sweep cycles are not simulated (modeled as standard SRAM).
- **Conditional Skip**: The `NUM` instruction (skip next instruction if Accumulator is negative) is not implemented in this simulator.
