# IBM 705 Architecture

- **Designer**: Werner Buchholz and team (at IBM)
- **Year Introduced**: 1954

## Unique Features
- **Character-Oriented Variable-Length Processing**: Primarily processed alphanumeric character strings instead of fixed-length binary words. Designed for business data operations.
- **Large Accumulator Capacity**: Main Accumulator could hold up to 256 characters; secondary Accumulator B was partitioned into 15 sections for flexible sub-field editing.
- **35-bit Instruction Format**: All instructions were exactly 5 characters of 7 bits each (35 bits), containing a 1-character opcode and 4-character address.

---

## Instruction Formats

In this simulator, each 35-bit instruction is structured as:
```text
 34          28 27                                0
+--------------+------------------------------------+
|    Opcode    |              Address               |
+--------------+------------------------------------+
```
- **Opcode** (bits 28-34): 7-bit opcode representing one character.
- **Address** (bits 0-27): Address path, simulated using 16 bits in the CPU core.

---

## Implemented Instructions
- **LD addr** (0x01): Load value from memory `addr` into Accumulator.
- **ST addr** (0x02): Store Accumulator value to memory `addr`.
- **ADD addr** (0x03): Add value at memory `addr` to Accumulator.
- **SUB addr** (0x04): Subtract value at memory `addr` from Accumulator.
- **HLT** (0x05): Halt processor execution.

---

## Unimplemented Instructions / Features
- **Accumulator Partitioning**: Partitioned Accumulator B and multi-character editing logic are not simulated.
- **Variable Field Length Scans**: Character-by-character memory scans are simplified to standard 35-bit parallel reads and writes.
- **Auxiliary Registers**: Non-arithmetic control registers and auxiliary storage units are omitted.
