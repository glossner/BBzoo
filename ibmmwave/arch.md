# IBM MWave Architecture

- **Designers**: IBM
- **Year Introduced**: 1992

## Unique Features
- **Integrated PC DSP**: MWave was designed not just as a standalone DSP but as an integrated solution for PCs, combining MIDI synthesis, audio codec functions, and modem processing.
- **General-Register Datapath**: Unlike accumulator-based DSPs, MWave employs a general-register set to ease integration with host PC architectures and programming languages.
- **Hardware Multitasking support**: Features real-time multitasking capabilities to handle multiple parallel multimedia tasks (e.g. concurrent modem and audio processing).

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
  - **LD R1, addr** (`0x30`): Load register R1 from the 16-bit absolute address.
  - **LD R2, addr** (`0x31`): Load register R2 from the 16-bit absolute address.
  - **ST R1, addr** (`0x33`): Store register R1 to the 16-bit absolute address.
- **Arithmetic**:
  - **ADD** (`0x32`): Add registers R1 and R2, placing the result in R1.
- **Control Flow**:
  - **HALT** (`0x00`): Halts the machine.

---

## Unimplemented Instructions / Features
- **General-purpose Register File**: Alternate register groups are omitted.
- **Direct Memory Access (DMA) & PC Bus Interface**: Host interface logic, ISA/PCI bus interface, and DMA controllers are not modeled.
- **Hardware MIDI/Modem synthesis logic**: Dedicate hardware accelerators are not simulated.

## Architectural Design Purpose

High-speed desktop multimedia audio, telephony, and dial-up modem signal processing.

## Target Purpose Stress Program

```assembly
# IBM MWave DSP arithmetic: y = x + a
LD R1, 20
LD R2, 21
ADD
ST R1, 22
```
