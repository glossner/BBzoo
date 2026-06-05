# JVM Architecture

- **Designers**: Sun Microsystems
- **Year Introduced**: 1995

## Unique Features
- **Stack-Based Architecture**: The Java Virtual Machine (JVM) is a classic stack-based architecture where operations evaluate on an operand stack instead of general-purpose registers.
- **Compact Bytecode**: Traditional JVM instructions are byte-aligned (variable length, e.g., 8-bit opcode followed by arguments). In our simplified model, instruction words are aligned to 32-bit values for memory consistency, while keeping the stack-based semantic.
- **Evaluation Stack**: Operands are pushed onto a LIFO evaluation stack (depth 16 in our simulation) and arithmetic instructions pop their operands, perform calculation, and push the result back onto the stack.

---

## Instruction Formats

JVM instructions are 32-bit words, with absolute address parameters occupying a second word:

### 1-Word Instructions (Stack Arithmetic and Halts)
```text
 31         24 23                                      0
+-------------+-----------------------------------------+
|   Opcode    |                 Unused                  |
+-------------+-----------------------------------------+
```

### 2-Word Instructions (Stack Loads and Stores)
```text
 Word 1:
 31         24 23                                      0
+-------------+-----------------------------------------+
|   Opcode    |                 Unused                  |
+-------------+-----------------------------------------+
 Word 2:
 31                                         0
+---------------------------------------------+
|             32-bit Memory Address           |
+---------------------------------------------+
```

---

## Implemented Instructions
- **Data Movement**:
  - **ILOAD addr** (`0x15`): Loads a 32-bit value from the specified absolute memory address and pushes it onto the operand stack (occupies 2 words).
  - **ISTORE addr** (`0x36`): Pops the top value from the operand stack and stores it to the specified absolute memory address (occupies 2 words).
- **Arithmetic**:
  - **IADD** (`0x60`): Pops the top two 32-bit values from the stack, adds them, and pushes the result back onto the stack.
- **Control Flow**:
  - **HALT** (`0xFF`): Halts the machine.

---

## Unimplemented Instructions / Features
- **Local Variables & Constant Pool**: Complex indexing of local variables and constant pool resolution are not modeled.
- **Object/Reference Operations**: Heap allocation, garbage collection, and method invocation instructions are omitted.
- **Exception Handling**: Try-catch mechanisms and JVM execution frames are not simulated.
- **Pipelining**: Not modeled as part of the stack machine execution logic.

## Architectural Design Purpose

Designed as a platform-independent virtual machine execution environment to run Java class files on any computer architecture via a portable bytecode interpreter/JIT compiler.

## Target Purpose Stress Program

```assembly
# JVM Vector Addition
ILOAD valA0
ILOAD valB0
IADD
ISTORE valC0
```
