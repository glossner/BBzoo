# Babbage Analytical Engine Architecture

- **Designer**: Charles Babbage
- **Year Designed**: 1837 (First fully documented design)

## Unique Features
- **Pioneering Mechanical Decimal Design**: Designed in the 1830s by Charles Babbage, it is widely recognized as the first design for a general-purpose, Turing-complete mechanical computer.
- **Store and Mill Separation**: Divided memory and processing functionally:
  - **The Store**: Physical columns of decimal wheels that hold variables and constants.
  - **The Mill**: The central processing unit where arithmetic operations are physically calculated.
- **Card-Programmed Control**: Instructed via separate loops of physical cardboard punch cards:
  - **Operation Cards**: Specify the arithmetic operation to perform.
  - **Variable Cards**: Specify which memory columns in the Store should supply inputs or receive results.

---

## Instruction Formats

In this simulator, the card-based instruction format is represented inside a 64-bit word:
```text
 63                            32 31               16 15                 0
+--------------------------------+-------------------+--------------------+
|             Opcode             |     Reserved      |      Address       |
+--------------------------------+-------------------+--------------------+
```
- **Opcode** (bits 32-63): Represents the Operation Card (1 = Load, 2 = Store, 3 = Add, 4 = Sub, 5 = Halt).
- **Address** (bits 0-15): Represents the Variable Card, designating the column address in the Store.

---

## Implemented Instructions
- **L addr** (0x01): Load value from Store column `addr` into the Mill register.
- **S addr** (0x02): Store the current value from the Mill register into Store column `addr`.
- **ADD addr** (0x03): Add the value from Store column `addr` to the Mill register.
- **SUB addr** (0x04): Subtract the value from Store column `addr` from the Mill register.
- **HLT** (0x05): Halt processor.

---

## Unimplemented Instructions / Features
- **Decimal Mechanical Precision**: Babbage's design used decimal gears with 50 digits of precision, which is simplified to 64-bit binary integers in this simulator.
- **Multiplication and Division**: The complex mechanical multiplication and division gears of the Mill are not implemented.
- **Conditional Card Stepping**: The ability to step the variable/operation card loops backward or forward (branches) based on sign and zero outcomes of Mill operations is not simulated.

## Architectural Design Purpose

Automating mathematical table calculations (like polynomials and logarithms) mechanically to eliminate human computation errors.

## Target Purpose Stress Program

```assembly
# Babbage Polynomial Evaluation: y = (x + a) - b
L 20    # Load x from Store column 20
ADD 21  # Add constant a from Store column 21
SUB 22  # Subtract constant b from Store column 22
S 23    # Store y to Store column 23
HLT
```
