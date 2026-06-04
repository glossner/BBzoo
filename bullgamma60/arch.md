# Bull Gamma 60 Architecture

- **Designer**: Compagnie des Machines Bull
- **Year Introduced**: 1960

## Unique Features
- **Pioneering Parallelism (Explicit Multithreading)**: Generally recognized as the world's first multi-threaded processing mainframe. It uses a central "Program Distributor" to route asynchronous execution streams to concurrent specialized functional units.
- **FORK and JOIN Directives**: Implements hardware-level fork and join mechanisms to coordinate multiple instruction streams (threads) concurrently.
- **Flexible Word Layout**: Operates on 24-bit instruction words and supports multi-word precision representations.

---

## Instruction Formats

The Bull Gamma 60 instruction is formatted as follows in this simple 24-bit core:
```text
 23          18 17    16 15                               0
+--------------+--------+----------------------------------+
|    Opcode    |  Reg   |             Address              |
+--------------+--------+----------------------------------+
```
- **Opcode** (bits 18-23): 6-bit instruction opcode.
- **Reg** (bits 16-17): 2-bit register selection (0 for `acc0`, 1 for `acc1`).
- **Address** (bits 0-15): 16-bit target memory location or branch/fork address.

---

## Implemented Instructions
- **LD reg, addr** (opcode 1): Load register `reg` with value from `addr`.
- **ADD reg, addr** (opcode 2): Add value from `addr` to register `reg`.
- **ST reg, addr** (opcode 3): Store value of register `reg` to `addr`.
- **FORK addr** (opcode 4): Spawn a concurrent execution thread starting at `addr`.
- **JOIN** (opcode 5): Thread synchronization. If executed on thread 1, it terminates thread 1. If executed on thread 0, it blocks until thread 1 terminates.
- **HLT** (opcode 6): Halts the main CPU thread.

---

## Unimplemented Instructions / Features
- **Concurrent Specialized Functional Units**: The physical division into separate BCD ALU, Binary ALU, translation unit, and sorting unit is not simulated.
- **Dynamic Queue Allocation**: Central hardware queue-management buffer in the Program Distributor is simplified to a single secondary thread register.
- **Variable Length Multi-Word Opcodes**: Multi-character instructions requiring consecutive word fetches are not modeled.
