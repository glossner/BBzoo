# HP 3000 Architecture

- **Designers**: Hewlett-Packard (HP)
- **Year Introduced**: 1972

## Unique Features
- **Stack-based Datapath**: HP 3000 is a classic stack-based minicomputer where most instructions operate implicitly on the top elements of the stack.
- **16-bit Architecture**: Standard 16-bit word size and 16-bit virtual address width.
- **Segmented Memory**: Code and data are strictly separated into different segments, accessed using segment registers.

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

### 2-Word Instructions (PUSH / POP absolute)
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
  - **PUSH addr** (`0x20`): Load value from 16-bit absolute address and push it onto the stack.
  - **POP addr** (`0x40`): Pop the top of the stack and store it at the 16-bit absolute address.
- **Arithmetic**:
  - **ADD** (`0x60`): Pop the two top values of the stack, add them, and push the result.
- **Control Flow**:
  - **HALT** (`0x00`): Halts the machine.

---

## Unimplemented Instructions / Features
- **Memory Segmentation**: Segmented registers (PB, PL, DL, DB, etc.) are not modeled.
- **Register Set**: Index registers (X) and status registers (SR) are not simulated.
- **Subroutines & Stack frames**: Stack frame pointers and frame setup instructions (PCAL, EXIT) are not simulated.

## Architectural Design Purpose

Multiprogramming and general-purpose system computing utilizing a compiler-friendly stack-based OS execution model.

## Target Purpose Stress Program

```assembly
# HP 3000 Stack expression evaluation: y = x + a
PUSH 20   # Push x
PUSH 21   # Push a
ADD       # Add (x + a)
POP 22    # Pop and store to y
HALT
```
