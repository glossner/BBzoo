# Burroughs B5500 Architecture

- **Designer**: Burroughs Corporation (led by Robert S. Barton)
- **Year Introduced**: 1964

## Unique Features
- **0-Address Stack Architecture**: Possesses no general-purpose data or address registers. All mathematical and logical operations are performed on an evaluation stack.
- **Top of Stack (TOS) Registers**: The top two elements of the stack are represented by high-speed hardware registers ($A$ and $B$). The CPU automatically manages pushes and pops between these registers and memory.
- **Descriptor-based Memory**: Uses specialized "descriptor" words to reference data segments and subroutines, providing hardware-level bounds checking and memory virtualization.
- **High-Level Language Co-Design**: One of the first computer families co-designed directly with a high-level language compiler (specifically Burroughs Extended ALGOL 60), meaning the machine has no assembler-level manual register management.

---

## Instruction Formats

The Burroughs B5500 utilizes 48-bit word structures. An instruction word can pack multiple "syllables" (opcodes). In this simplified core, the instruction uses the following format:
```text
 47      40 39                     25 24                 0
+----------+-------------------------+--------------------+
|  Opcode  |         Address         |      Reserved      |
+----------+-------------------------+--------------------+
```
- **Opcode** (bits 40-47): 8-bit instruction syllable.
- **Address** (bits 25-39): 15-bit memory target (used for PUSH/POP).

---

## Implemented Instructions
- **PUSH addr** (0x01): Load value from memory address `addr` and push onto the top of the stack.
- **POP addr** (0x02): Pop value from the top of the stack and store it in memory address `addr`.
- **ADD** (0x03): Pop the top two elements ($A$ and $B$), add them ($A + B$), and push the result back onto the stack.
- **SUB** (0x04): Pop the top two elements ($A$ and $B$), subtract them ($B - A$), and push the result back onto the stack.
- **HLT** (0x05): Halt processor.

---

## Unimplemented Instructions / Features
- **Descriptor Management**: Array descriptor parsing, presence bits, and hardware bound checking are not implemented.
- **Automatic Stack Spilling**: Hardware-managed stack frame allocation, display registers (D0-D31 for nested lexical scope pointers), and memory spilling of TOS registers are not simulated.
- **Data Tagging**: Word-level tag bits identifying integers, single/double-precision floats, program descriptors, and control words are not implemented.
- **ALGOL Subroutine Calls**: Nested block entry/exit and program control words (RCW, MSCW) are not simulated.
