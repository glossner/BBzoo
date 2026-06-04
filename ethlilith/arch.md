# Lilith Architecture

- **Designers**: Niklaus Wirth
- **Year Introduced**: 1980

## Unique Features
- **High-Level Language Oriented**: Lilith was designed specifically to execute Wirth's Modula-2 M-code, matching compiler constructs closely to hardware operations.
- **Stack-based Evaluation**: Operands are evaluated on an evaluation stack, minimizing instruction size and compiler complexity.
- **16-bit Workstation Architecture**: 16-bit word size and byte addressing.

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
  - **PUSH addr** (`0x10`): Load value from 16-bit absolute address and push it onto the stack.
  - **POP addr** (`0x30`): Pop the top of the stack and store it at the 16-bit absolute address.
- **Arithmetic**:
  - **ADD** (`0x50`): Pop the two top values of the stack, add them, and push the result.
- **Control Flow**:
  - **HALT** (`0x00`): Halts the machine.

---

## Unimplemented Instructions / Features
- **M-code Instructions**: The vast majority of Modula-2 M-code bytecodes (LFC, LI, LGB, etc.) are unimplemented.
- **Local/Global Frame Addressing**: Frame offsets and dynamic links are not modeled.
- **Display Register Stack**: The display registers for nesting scopes are not simulated.
