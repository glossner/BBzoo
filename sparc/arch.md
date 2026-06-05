# SPARC Architecture

- **Designers**: Sun Microsystems
- **Year Introduced**: 1987

## Unique Features
- **Register Windowing**: SPARC (Scalable Processor Architecture) is famous for its register windowing scheme (though simplified here to global registers).
- **32-bit RISC**: Employs a 32-bit data path, 32-bit memory address space, and 32 general-purpose 32-bit registers mapped as global (%g0-%g7), output (%o0-%o7), local (%l0-%l7), and input (%i0-%i7).
- **Strict Load/Store Paradigm**: Separates memory access from data operations, ensuring arithmetic only operates on registers.

---

## Instruction Formats

SPARC instructions are 32-bit words, with absolute address parameters occupying a second word:

### 1-Word Instructions (Register-to-Register and Halts)
```text
 31         26 25     21 20     16 15     11 10        0
+-------------+---------+---------+---------+-----------+
|   Opcode    |  Reg RD |  Reg RS1|  Reg RS2|  Unused   |
+-------------+---------+---------+---------+-----------+
```

### 2-Word Instructions (Load and Store absolute)
```text
 Word 1:
 31         26 25     21 20     16 15                 0
+-------------+---------+---------+---------------------+
|   Opcode    | Unused  |  Reg RS1|       Unused        |
+-------------+---------+---------+---------------------+
 Word 2:
 31                                         0
+---------------------------------------------+
|             32-bit Memory Address           |
+---------------------------------------------+
```

---

## Implemented Instructions
- **Data Movement**:
  - **LD [addr], Rd** (`0x02`): Load register Rd from 32-bit absolute address (occupies 2 words).
  - **ST Rs, [addr]** (`0x03`): Store register Rs to 32-bit absolute address (occupies 2 words).
- **Arithmetic**:
  - **ADD Rs1, Rs2, Rd** (`0x01`): Add Rs1 and Rs2, store result in Rd.
- **Control Flow**:
  - **HALT** (`0x3F`): Halts the machine.

---

## Unimplemented Instructions / Features
- **Register Windows**: The active register window pointer (CWP) and window overflow/underflow traps are not modeled.
- **Branches and Jumps**: Branch instructions and condition codes are omitted.
- **Caches & Pipelining**: Memory and instruction caches are not simulated.

## Architectural Design Purpose

Designed as a scalable processor for high-performance workstations and servers.

## Target Purpose Stress Program

```assembly
# SPARC Vector Addition
LD [valA0], %g0
LD [valB0], %g1
ADD %g0, %g1, %g0
ST %g0, [valC0]
```
