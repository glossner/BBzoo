# CDC 6600 Architecture

- **Designer**: Seymour Cray (for Control Data Corporation)
- **Year Introduced**: 1964

## Unique Features
- **60-bit Word Size**: Designed by Seymour Cray, the CDC 6600 is often considered the first successful supercomputer, operating with massive 60-bit floating-point/data words and 18-bit address paths.
- **Three Separate Register Files**: Features three distinct register groups:
  - **X Registers** ($X0$-$X7$, 60-bit): General operand/data registers.
  - **A Registers** ($A0$-$A7$, 18-bit): Address registers.
  - **B Registers** ($B0$-$B7$, 18-bit): Index/increment registers ($B0$ is hardwired to 0).
- **Implicit Memory Loads and Stores**: Access to memory is performed as a side-effect of modifying an Address ($A$) register:
  - Setting $A_i$ ($1 \le i \le 5$) automatically loads the 60-bit word at that address into register $X_i$.
  - Setting $A_6$ or $A_7$ automatically stores the 60-bit word from $X_6$ or $X_7$ to that address.
  - Modifying $A_0$ has no memory side-effects.
- **Instruction Packing**: Allows multiple 15-bit or 30-bit instructions to be packed within a single 60-bit memory word.

---

## Instruction Formats

CDC 6600 instructions are either 15-bit or 30-bit. The 30-bit instruction format (incorporating an 18-bit constant $K$) is shown below:
```text
 29      24 23   21 20   18 17   15 14                        0
+----------+-------+-------+-------+--------------------------+
|  Opcode  |   i   |   j   |   k   |            K             |
+----------+-------+-------+-------+--------------------------+
```
- **Opcode** (bits 24-29): 6-bit operation code.
- **i** (bits 21-23): Target/destination register.
- **j** (bits 18-20): First source register.
- **k** (bits 15-17): Second source register.
- **K** (bits 0-14): 18-bit unsigned address/constant (only present in 30-bit format).

---

## Implemented Instructions
- **Address Modifications** (Implicit Load/Store):
  - **Ai = Bj + K** (0x01): Compute address ($A_i \leftarrow B_j + K$).
    - Triggers load ($X_i \leftarrow \text{mem}[A_i]$) if $1 \le i \le 5$.
    - Triggers store ($\text{mem}[A_i] \leftarrow X_i$) if $i = 6$ or $7$.
- **Operand Math**:
  - **Xi = Xj + Xk** (0x02): Add 60-bit register $X_j$ to $X_k$ and store in $X_i$.
  - **Xi = Xj - Xk** (0x03): Subtract 60-bit register $X_k$ from $X_j$ and store in $X_i$.
- **Control**:
  - **HLT**: Stop execution.

---

## Unimplemented Instructions / Features
- **Scoreboard and Parallel Units**: The centralized Scoreboard (for managing read/write conflicts and out-of-order execution) and the 10 parallel functional units are not simulated.
- **Instruction Packing**: The instruction pipeline does not unpack multiple instructions from a single 60-bit word (each memory word holds exactly one instruction).
- **Float and Logical Units**: Floating-point multiplication, division, normalize, population count, and logical bitwise operations are not implemented.
- **Conditional Branches**: Jump/branch instructions based on comparison of registers are not implemented.

## Architectural Design Purpose

Pioneering scientific supercomputing with parallel functional units and address-register-triggered load/store logic.

## Target Purpose Stress Program

```assembly
# CDC 6600 Parallel evaluation: y = x + a
A1 = B0 + 20   # Load x into X1
A2 = B0 + 21   # Load a into X2
X6 = X1 + X2   # X6 = x + a
A6 = B0 + 22   # Store X6 to address 22
```
