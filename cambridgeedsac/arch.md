# EDSAC Architecture

- **Designer**: Maurice Wilkes and team (at Cambridge University)
- **Year Introduced**: 1949

## Unique Features
- **First Practical Stored-Program Computer**: The first fully functional stored-program electronic computer to enter regular service.
- **Serial Datapath & Mercury Delay Lines**: Memory utilized mercury delay lines storing acoustic pulses; logic operated on bit-serial formats.
- **No Direct Load Instruction**: Lacked a direct "load" instruction. To load a value, the accumulator was first cleared (e.g., using `T` to a dummy address) and then the value was added to the accumulator via `A`.
- **17-bit Word Structure**: Addressed memory as 17-bit single words (or 35-bit double words).

---

## Instruction Formats

In this simulator, each 17-bit instruction is formatted as:
```text
 16          10 9                  0
+--------------+--------------------+
|    Opcode    |      Address       |
+--------------+--------------------+
```
- **Opcode** (bits 10-16): 7-bit instruction opcode.
- **Address** (bits 0-9): 10-bit target memory location.

---

## Implemented Instructions
- **A addr** (0x01): Add value at memory address `addr` to Accumulator.
- **S addr** (0x02): Subtract value at memory address `addr` from Accumulator.
- **T addr** (0x03): Transfer (store) Accumulator value to memory address `addr` and clear Accumulator.
- **U addr** (0x04): Transfer (store) Accumulator value to memory address `addr` without clearing Accumulator.
- **Z** (0x05): Halt / stop processor.

---

## Unimplemented Instructions / Features
- **Bit-Serial Memory Timing**: Delay line timing effects are not simulated; memory accesses are instantaneous and parallel.
- **Double Words (35-bit)**: double-precision modes are not implemented.
- **Hardware Multiplication**: Multiplier register operations (`V`, `N`) are not simulated.
- **Shift & Round**: Shift (`R`, `L`) and round operations are omitted.
- **Input/Output**: Paper tape reader and teleprinter instructions (`I`, `O`, `F`, `G`) are not simulated.
