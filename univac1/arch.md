# Univac I Architecture

- **Designers**: J. Presper Eckert and John Mauchly
- **Year Introduced**: 1951

## Unique Features
- **First Commercial Alphanumeric Computer**: Developed in 1951 by J. Presper Eckert and John Mauchly, it was the first commercially successful US computer, processing both numeric and alphabetical characters.
- **72-Bit Alphanumeric Words**: Operated on 72-bit words, representing 12 alphanumeric characters encoded in a 6-bit excess-3 format.
- **Mercury Delay Line Memory**: Main memory consisted of acoustic delay lines using tubes of liquid mercury to store pulses serially, which recirculated continuously.
- **Instruction Packing**: The standard design packed two 36-bit instructions (each consisting of a 1-character opcode and a 3-digit decimal address) into a single 72-bit word.

---

## Instruction Formats

In this simulator, the 72-bit instruction word is formatted as:
```text
 71          64 63                               16 15                 0
+--------------+-----------------------------------+--------------------+
|    Opcode    |             Reserved              |      Address       |
+--------------+-----------------------------------+--------------------+
```
- **Opcode** (bits 64-71): 8-bit character representation of instruction (1 = B, 2 = H, 3 = A, 4 = S, 5 = Q).
- **Address** (bits 0-15): 16-bit target memory location.

---

## Implemented Instructions
- **B addr** (0x01): Bring / Load value at `addr` into Accumulator $A$.
- **H addr** (0x02): Hold / Store value of Accumulator $A$ to memory address `addr`.
- **A addr** (0x03): Add value at memory address `addr` to Accumulator $A$ ($A \leftarrow A + \text{mem}[addr]$).
- **S addr** (0x04): Subtract value at memory address `addr` from Accumulator $A$ ($A \leftarrow A - \text{mem}[addr]$).
- **Q** (0x05): Stop / Halt.

---

## Unimplemented Instructions / Features
- **Instruction Unpacking**: The instruction register does not split 72-bit memory words into two 36-bit instructions; each 72-bit word holds a single instruction.
- **Excess-3 and Alphanumeric Math**: The 6-bit excess-3 representation and sign/decimal alignment are simplified to standard 72-bit binary integer math.
- **Mercury Tube Recirculation Latency**: The serial access times of mercury delay lines are not simulated (memory behaves as random-access SRAM).
- **Tape I/O (UNISERVO)**: Magnetic tape system reads and writes are not simulated.

## Architectural Design Purpose

Large-scale commercial data processing, business accounting, and census tabulation.

## Target Purpose Stress Program

```assembly
# Univac I Polynomial Evaluation: y = (x + a) - b
B 20    # Load x into accumulator
A 21    # Add a to accumulator
S 22    # Subtract b from accumulator
H 23    # Store y
Q       # Quit
```
