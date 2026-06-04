# IBM 1401 Architecture

- **Designer**: Chuck Branscomb and team (at IBM)
- **Year Introduced**: 1959

## Unique Features
- **Variable Word Length & Word Marks**: Instead of fixed register widths, data and instructions spanned variable memory locations. The end of a field or instruction was defined by a special "word mark" bit associated with each BCD character.
- **Memory-to-Memory CISC Architecture**: Lacked an accumulator register for arithmetic operations. Instead, instructions executed arithmetic and data movement directly between source and destination memory addresses.
- **8-bit Alphanumeric Characters**: Memory stored 8-bit bytes (6-bit BCD character, 1-bit word mark, and 1-bit parity).

---

## Instruction Formats

In this simulator, each instruction is structured as a 36-bit word:
```text
 35          24 23          12 11                 0
+--------------+--------------+--------------------+
|    Opcode    |   A-Address  |     B-Address      |
+--------------+--------------+--------------------+
```
- **Opcode** (bits 24-35): 12-bit opcode field, simulated with 8 bits.
- **A-Address** (bits 12-23): 12-bit source memory address.
- **B-Address** (bits 0-11): 12-bit destination memory address.

---

## Implemented Instructions
- **MC A_addr, B_addr** (0x01): Move Character. Copies value from `A_addr` to `B_addr` in memory.
- **A A_addr, B_addr** (0x02): Add. Computes `mem(B_addr) = mem(B_addr) + mem(A_addr)`.
- **S A_addr, B_addr** (0x03): Subtract. Computes `mem(B_addr) = mem(B_addr) - mem(A_addr)`.
- **HLT** (0x04): Halt processor execution.

---

## Unimplemented Instructions / Features
- **Word Mark Logic**: Word mark setting (`SW`), clearing (`CW`), and checking during arithmetic carry propagation are not simulated; instructions operate on 36-bit parallel word fields.
- **Variable Instruction Lengths**: Physical 1401 instructions spanned 1, 2, 4, 5, 7, or 8 characters; the simulator uses fixed 36-bit (1-word) instructions.
- **Modifier Characters**: Instruction modifier fields (`d-modifier`) are not simulated.

## Architectural Design Purpose

Pervasive commercial business data processing, card manipulation, and high-speed report printing.

## Target Purpose Stress Program

```assembly
# IBM 1401 Polynomial Evaluation: y = x + a (memory-to-memory copy and arithmetic)
MC 20, 22  # Move character x to y
A 21, 22   # Add character a to y
HLT
```
