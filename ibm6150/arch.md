# IBM 6150 ROMP Architecture

- **Designers**: John Cocke and the IBM Team
- **Year Introduced**: 1986

## Unique Features
- **Early Commercial RISC Design**: The ROMP (Research/OPD MicroProcessor) is one of the earliest commercial RISC (Reduced Instruction Set Computer) processors, drawing heavily from the pioneering IBM 801 RISC research.
- **32-bit Architecture**: Employs a 32-bit data path, 32-bit memory address space, and 16 general-purpose 32-bit registers ($R0$ through $R15$).
- **Orthogonal Load/Store Concept**: Memory accesses are strictly separated from data manipulation. Computational instructions only operate on registers, while dedicated Load ($L$) and Store ($ST$) instructions move data between registers and memory.

---

## Instruction Formats

IBM 6150 ROMP instructions in this model are 32-bit words, with absolute address parameters occupying a second word:

### 1-Word Instructions (Register-to-Register and Halts)
```text
 31         24 23     20 19     16 15        0
+-------------+---------+---------+-----------+
|   Opcode    |  Reg X  |  Reg Y  |  Unused   |
+-------------+---------+---------+-----------+
```

### 2-Word Instructions (Load and Store absolute)
```text
 Word 1:
 31         24 23     20 19                 0
+-------------+---------+---------------------+
|   Opcode    |  Reg X  |       Unused        |
+-------------+---------+---------------------+
 Word 2:
 31                                         0
+---------------------------------------------+
|             32-bit Memory Address           |
+---------------------------------------------+
```

---

## Implemented Instructions
- **Data Movement**:
  - **L Rx, addr** (`0x80`): Load register Rx from 32-bit absolute address (occupies 2 words).
  - **ST Rx, addr** (`0x90`): Store register Rx to 32-bit absolute address (occupies 2 words).
- **Arithmetic**:
  - **A Rx, Ry** (`0xA0`): Add register Ry to Rx (result in Rx).
- **Control Flow**:
  - **HALT** (`0x00`): Halts the machine.

---

## Unimplemented Instructions / Features
- **16-bit ROMP Instructions**: The historical ROMP has a mixed 16-bit/32-bit instruction encoding; only the 32-bit forms are modeled here.
- **Register-Indirect & Displacement Addressing**: Address calculation modes such as register plus displacement, indexed addressing, and autoincrement are unimplemented.
- **Floating Point Co-processor**: FPU registers and decimal operations are not simulated.
- **Virtual Memory & MMU**: The memory management unit (which featured an advanced inverted page table design) is not modeled.

## Architectural Design Purpose

High-performance computer-aided design (CAD) workstations utilizing early pipelined RISC execution paradigms.

## Target Purpose Stress Program

```assembly
# IBM 6150 RT PC arithmetic: y = x + a
L R0, 20
L R1, 21
A R0, R1
ST R0, 22
```
