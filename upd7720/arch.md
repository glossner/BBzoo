# NEC uPD7720 Architecture

- **Designers**: NEC
- **Year Introduced**: 1980

## Unique Features
- **First Single-Chip DSP with Hardware Multiplier**: µPD7720 pioneered the integration of a dedicated high-speed 16x16-bit multiplier on-chip.
- **Harvard Architecture**: Separate program memory (ROM) and data memory (RAM), allowing simultaneous fetch of instructions and operand data.
- **Dual Accumulators**: Features two 16-bit accumulator registers (Acc A and Acc B) to allow double-buffered or pipelined calculations.

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
  - **LD A, addr** (`0x10`): Load value from 16-bit absolute address into Accumulator A.
  - **LD B, addr** (`0x11`): Load value from 16-bit absolute address into Accumulator B.
  - **ST A, addr** (`0x13`): Store value from Accumulator A to the 16-bit absolute address.
- **Arithmetic**:
  - **ADD** (`0x12`): Add Accumulator B to Accumulator A and store the result in Accumulator A.
- **Control Flow**:
  - **HALT** (`0x00`): Halts the machine.

---

## Unimplemented Instructions / Features
- **Parallel Execution Fields**: The microinstruction control word fields (which control RAM/ROM addresses, ALU ops, and pointer moves concurrently in a single instruction) are not modeled.
- **Address Registers & DAG**: Data pointer registers (DP, RP) and auto-increment/decrement logic are unimplemented.
- **Serial Interface**: The serial input/output shift registers and interrupts are not simulated.

## Architectural Design Purpose

Voice-band telecom signal filter operations and real-time modem processing.

## Target Purpose Stress Program

```assembly
# NEC uPD7720 DSP arithmetic: y = x + a
LD A, 20
LD B, 21
ADD
ST A, 22
```
