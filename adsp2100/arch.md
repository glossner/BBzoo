# ADI ADSP-2100 Architecture

- **Designers**: Analog Devices (ADI)
- **Year Introduced**: 1986

## Unique Features
- **Parallel Computation Units**: The ADSP-2100 family integrates three independent computational units: the ALU, the Multiplier-Accumulator (MAC), and the Barrel Shifter.
- **Harvard Architecture**: Explicit separation of Program Memory (PM) and Data Memory (DM) with independent buses, permitting instructions and data operands to be fetched simultaneously in a single cycle.
- **General-Register Datapath**: Features multiple input/output registers (like AX0, AX1, AY0, AY1, AR) for computational units rather than a single accumulator.

---

## Instruction Formats

In our simulation, absolute load/store instructions span 2 words where the target address follows the instruction word, while register/stack computational instructions occupy 1 word.

### 1-Word Instructions (ADD / HALT)
```text
 15        8 7        0
+-----------+----------+
|  Opcode   |  Unused  |
+-----------+----------+
```

### 2-Word Instructions (LD / ST absolute)
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
  - **LD AX0, addr** (`0x20`): Load register AX0 from the 16-bit absolute address.
  - **LD AY0, addr** (`0x21`): Load register AY0 from the 16-bit absolute address.
  - **ST AR, addr** (`0x23`): Store register AR to the 16-bit absolute address.
- **Arithmetic**:
  - **ADD AR** (`0x22`): Add registers AX0 and AY0, placing the result in AR.
- **Control Flow**:
  - **HALT** (`0x00`): Halts the machine.

---

## Unimplemented Instructions / Features
- **Secondary Registers**: Alternative register banks (AX1, AY1, MX0, MY0, etc.) are omitted.
- **Data Address Generators (DAGs)**: DAG1 and DAG2 with post-modify and bit-reverse addressing features are not implemented.
- **Hardware MAC and Shifters**: The MAC hardware and multi-bit barrel shifter are simplified.
