# Harvard Mark I Architecture

- **Designer**: Howard Aiken (in collaboration with IBM engineers Clair Lake, Benjamin Durfee, Frank Hamilton)
- **Year Introduced**: 1944

## Unique Features
- **Electromechanical Relay Design**: Built in 1944 by Howard Aiken and IBM, utilizing electromagnetic relays, mechanical shafts, and gear assemblies.
- **Out-Field / In-Field Instruction Format**: Programs were read sequentially from a 24-hole wide paper tape. Instructions were divided into three main fields:
  - **Out-Field**: Designated the source register or input device.
  - **In-Field**: Designated the destination register or output device.
  - **Operational-Field**: Designated the operation (Add, Sub, etc.) to perform during the transfer.
- **72 Accumulators**: Had 72 mechanical accumulator registers, each capable of holding 23 decimal digits plus a sign wheel.
- **Pioneering Harvard Architecture**: Established the complete physical separation between instruction storage (sequential paper tape) and data storage (rotary switches and register wheels).

---

## Instruction Formats

In this simulator, the 24-bit Out/In/Op tape format is represented inside a 64-bit instruction word:
```text
 63                            24 23         16 15          8 7          0
+--------------------------------+-------------+-------------+------------+
|            Reserved            |   Opcode    |   Source    |    Dest    |
+--------------------------------+-------------+-------------+------------+
```
- **Opcode** (bits 16-23): Operational field (1 = MOV, 2 = ADD, 3 = SUB, 4 = HLT).
- **Source** (bits 8-15): Out-field register address (0-71).
- **Dest** (bits 0-7): In-field register address (0-71).

---

## Implemented Instructions
- **MOV src, dst** (0x01): Move the 64-bit value from register `src` to register `dst`.
- **ADD src, dst** (0x02): Add value from register `src` to register `dst` ($dst \leftarrow dst + src$).
- **SUB src, dst** (0x03): Subtract value of register `src` from register `dst` ($dst \leftarrow dst - src$).
- **HLT** (0x04): Halt processor.

---

## Unimplemented Instructions / Features
- **Mechanical decimal wheel simulation**: Simplified internally to 64-bit binary integers.
- **Specialized Interpolators**: Built-in electromechanical units for calculating trigonometric (sine, cosine) and logarithmic functions are not implemented.
- **Constant Switches**: The 60 manual rotary switches used to input hardcoded program constants are replaced by a synthesizable register test-write port.
