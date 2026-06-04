# UCSD Pascal P-Machine Architecture

- **Designers**: University of California, San Diego (UCSD)
- **Year Introduced**: 1978

## Unique Features
- **Virtual Stack Machine**: Unlike many stack architectures which were physical processors, the P-Machine was designed primarily as a virtual machine target for the Pascal compiler.
- **Portability Focus**: Mapped high-level Pascal constructs to stack bytecodes, making it easy to run UCSD Pascal on various microcomputers by writing a simple interpreter.
- **16-bit Design**: Standard 16-bit word length and byte addressing.

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
  - **PUSH addr** (`0x80`): Load value from 16-bit absolute address and push it onto the stack.
  - **POP addr** (`0x90`): Pop the top of the stack and store it at the 16-bit absolute address.
- **Arithmetic**:
  - **ADD** (`0xA0`): Pop the two top values of the stack, add them, and push the result.
- **Control Flow**:
  - **HALT** (`0x00`): Halts the machine.

---

## Unimplemented Instructions / Features
- **Pointer/Relative Addressing**: Instructions for accessing variables via static/dynamic links (LOD, STR, etc.) are not modeled.
- **Procedure Call / Return**: Activation record allocation and program jumps (MST, CUP, ENT, RET) are not simulated.
- **Set & Array operations**: Pascal-specific set arithmetic and array indexing (DEC, IND, etc.) are unimplemented.

## Architectural Design Purpose

Portable Pascal P-code execution in hardware to achieve cross-platform software distribution.

## Target Purpose Stress Program

```assembly
# UCSD Pascal P-Machine Stack evaluation: y = x + a
PUSH 20   # Push x
PUSH 21   # Push a
ADD       # Add (x + a)
POP 22    # Pop and store to y
HALT
```
