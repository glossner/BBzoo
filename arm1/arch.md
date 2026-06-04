# ARM1 Architecture

- **Designers**: Sophie Wilson and Steve Furber (Acorn Computers)
- **Year Introduced**: 1985

## Unique Features
- **Load-Store RISC Architecture**: Data processing operations are register-to-register only, and memory accesses are limited to explicit Load (LDR) and Store (STR) instructions.
- **Conditional Execution**: ARM1 is famous for allowing every instruction to be executed conditionally based on the status flags (although condition E - "Always" is the default).
- **Register File with PC integration**: ARM1 has 16 registers (R0-R15), where R15 is the Program Counter (PC), enabling branch and PC-relative addressing using standard register operations.

---

## Instruction Formats

In our simulation, absolute load/store instructions span 2 words where the target address follows the instruction word, while register computational instructions occupy 1 word.

### 1-Word Instructions (ADD)
```text
 31    28 27      20 19    16 15    12 11     4 3      0
+--------+----------+--------+--------+--------+--------+
|  Cond  |  Opcode  |   rn   |   rd   | Unused |   rm   |
+--------+----------+--------+--------+--------+--------+
```

### 2-Word Instructions (LDR / STR absolute)
```text
 Word 1:
 31    28 27      20 19    16 15                        0
+--------+----------+--------+--------+-----------------+
|  Cond  |  Opcode  | Unused |   rd   |     Unused      |
+--------+----------+--------+--------+-----------------+
 Word 2:
 31                                                     0
+-------------------------------------------------------+
|                 32-bit Target Address                 |
+-------------------------------------------------------+
```

---

## Implemented Instructions
- **Data Movement**:
  - **LDR rd, addr** (cond `0xE`, opcode `0x04`): Load Word from 32-bit absolute address into register rd.
  - **STR rd, addr** (cond `0xE`, opcode `0x05`): Store Word from register rd into 32-bit absolute address.
- **Arithmetic**:
  - **ADD rd, rn, rm** (cond `0xE`, opcode `0x00`): Add registers rn and rm and store result in rd.
- **Control Flow**:
  - **HALT** (cond `0xE`, opcode `0x0F`): Halts the machine.

---

## Unimplemented Instructions / Features
- **Barrel Shifter**: Opcode operands cannot specify barrel shifts (LSL, LSR, ASR, ROR) or immediate values.
- **Multiplication**: The MUL and MLA instructions are not modeled.
- **Branches**: Conditional branch instructions (B, BL) are not modeled.
- **Status Registers**: The Current Program Status Register (CPSR) and its flags (N, Z, C, V) are not simulated.
