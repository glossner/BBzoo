# Zuse Z1 Architecture

- **Designer**: Konrad Zuse
- **Year Completed**: 1938

## Unique Features
- **Pioneering Binary Mechanical Computer**: Designed and completed in 1938 by Konrad Zuse, it operated entirely on sliding metal sheets, rods, and pins, without relays or vacuum tubes.
- **First Binary Floating-Point Machine**: Used a 22-bit binary floating-point word (1-bit sign, 7-bit exponent, 14-bit mantissa) for all data computations.
- **Two-Register Datapath**: Computations were based on two primary internal floating-point registers: $R1$ and $R2$.
- **35mm Film Program Tape**: Read instructions sequentially from punched holes on standard 35mm movie film stock.

---

## Instruction Formats

In this simulator, the Zuse Z1 instructions are represented as 22-bit words:
```text
 21          16 15                               0
+--------------+---------------------------------+
|    Opcode    |             Address             |
+--------------+---------------------------------+
```
- **Opcode** (bits 16-21): Operation select (1 = Pr, 2 = Ps, 3 = ADD, 4 = SUB, 5 = MOV R1, R2, 6 = HLT).
- **Address** (bits 0-15): Memory location.

---

## Implemented Instructions
- **Pr addr** (0x01): Load memory value at `addr` into register $R1$.
- **Ps addr** (0x02): Store the value of register $R1$ into memory address `addr`.
- **ADD** (0x03): Add register $R2$ to $R1$ ($R1 \leftarrow R1 + R2$).
- **SUB** (0x04): Subtract register $R2$ from $R1$ ($R1 \leftarrow R1 - R2$).
- **MOV R1, R2** (0x05): Move register $R1$ value into register $R2$.
- **HLT** (0x06): Halt processor.

---

## Unimplemented Instructions / Features
- **Sliding-pin floating-point simulation**: The simulator simplifies calculations using 22-bit binary fixed-point integers rather than actual floating-point representation.
- **Input/Output Conversions**: The decimal keyboard (which converted input to binary float) and decimal lamp display (converting binary float to decimal output) are not simulated.
- **Multiply/Divide Operations**: Mechanical multiplication and division steps are not implemented.

## Architectural Design Purpose

Solving complex engineering calculations and structural formulas automatically using binary floating-point mechanical gates.

## Target Purpose Stress Program

```assembly
# Zuse Z1 Polynomial Evaluation: y = (x + a) - b
PR 20   # Load x into register R1
MOV R1, R2 # Move x to R2
PR 21   # Load a into R1
ADD     # R1 = x + a
MOV R1, R2 # Move sum to R2
PR 22   # Load b into R1
SUB     # R1 = (x + a) - b
PS 23   # Store result y to address 23
HLT
```
