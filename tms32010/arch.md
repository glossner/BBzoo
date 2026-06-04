# TI TMS32010 Architecture

- **Designers**: Texas Instruments (team led by Surendar Magar)
- **Year Introduced**: 1982

## Unique Features
- **Landmark General-Purpose DSP**: TMS32010 was a massive commercial success that established the long-running TMS320 family in industrial and consumer applications.
- **Modified Harvard Architecture**: Allows instructions to be stored in data memory (and vice versa) using instructions like TBLR (Table Read) and TBLW (Table Write), breaking strict separation to allow coefficients to be fetched dynamically.
- **16-bit Instruction & Datapath**: A 16-bit word size and a fast multiplier that feeds a 32-bit Accumulator (ACC) to prevent precision loss.

---

## Instruction Formats

In our simulation, absolute load/store instructions span 2 words where the target address follows the instruction word, while register/stack computational instructions occupy 1 word.

### 1-Word Instructions (HALT)
```text
 15        8 7        0
+-----------+----------+
|  Opcode   |  Unused  |
+-----------+----------+
```

### 2-Word Instructions (LAC / ADD / SACL absolute)
```text
 Word 1:
 15        8 7        0
+-----------+----------+
|  Opcode   |  Unused  |
+-----------+----------+
 Word 2:
 15                   0
+----------------------+
| 16-bit Target Address|
+----------------------+
```

---

## Implemented Instructions
- **Data Movement**:
  - **LAC addr** (`0x01`): Load Accumulator with the value from 16-bit absolute address (sign-extended to 32 bits).
  - **SACL addr** (`0x03`): Store Accumulator Low (low 16 bits of ACC) to the 16-bit absolute address.
- **Arithmetic**:
  - **ADD addr** (`0x02`): Add value from 16-bit absolute address (sign-extended) to the Accumulator.
- **Control Flow**:
  - **HALT** (`0x00`): Halts the machine.

---

## Unimplemented Instructions / Features
- **Table Read / Write**: The TBLR and TBLW instructions are not modeled.
- **Hardware Multiplier / Shifters**: The 16x16 hardware multiplier and input/output shifters are not simulated; instead, standard addition is used.
- **Auxiliary Registers (AR0, AR1)**: Pointer registers and auto-increment indirect addressing are unimplemented.
