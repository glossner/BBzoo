# CDC 6600 PPU Architecture

- **Designer**: Seymour Cray
- **Year Introduced**: 1964

## Unique Features
- **Peripheral Processing Unit (PPU)**: The CDC 6600 mainframe offloaded all I/O and operating system functions to a pool of 10 independent PPUs, leaving the Central Processor (CP) free for high-speed scientific calculation.
- **Barrel and Slot Pipeline**: The 10 PPUs shared a common instruction processing loop (the barrel and slot) which advanced instruction execution of one PPU every 100ns cycle, simulating 10 distinct processors with minimal hardware overhead.
- **12-Bit Word Size**: The PPU operates on 12-bit words, contrasting with the main CP's 60-bit word size.

---

## Instruction Formats

The CDC 6600 PPU uses a 12-bit instruction format:
```text
 11          6 5                0
+-------------+------------------+
|   Opcode    |      Address     |
+-------------+------------------+
```
- **Opcode** (bits 6-11): 6-bit instruction operation code.
- **Address** (bits 0-5): 6-bit direct address pointing to locations 0–63 in local memory.

---

## Implemented Instructions
- **LD d** (opcode 01 octal/decimal): Load: copies the contents of memory location `d` into the Accumulator (ACC).
- **ADD d** (opcode 02 octal/decimal): Add: adds the contents of memory location `d` to ACC.
- **ST d** (opcode 03 octal/decimal): Store: copies the contents of ACC into memory location `d`.
- **SUB d** (opcode 04 octal/decimal): Subtract: subtracts the contents of memory location `d` from ACC.
- **HLT** (opcode 05 octal/decimal): Halts the PPU.

---

## Unimplemented Instructions / Features
- **18-bit Addressing / Long Format**: 24-bit instruction formats (using two 12-bit words) for addressing up to 4096 memory words directly or relative to index registers are not implemented.
- **Channel I/O**: Channel transfer instructions (such as IAM, OAM, ACN, etc.) for communication with central memory and external I/O channels are not simulated.
- **Register Operations**: Operations involving non-ACC index registers or PPU register swapping are not simulated.

## Architectural Design Purpose

Offloading input/output (I/O) processing and network control from the main central processor of the supercomputer.

## Target Purpose Stress Program

```assembly
# CDC 6600 PPU I/O Processing: y = x + a
LD 20
ADD 21
ST 22
```
