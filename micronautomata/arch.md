# Micron Automata Processor 🏛️

## Introduction
* **Designer/Team:** Micron Technology
* **Year:** 2013
* **Paradigm:** Automata Processing in Memory (AP-PIM)

## Unique Architectural Features
- **State-Transition Processing:** Repurposes memory row and column arrays to execute massive numbers of state-transition engines in parallel.
- **Graph & Pattern Matching:** Designed specifically for graph processing, regular expression matching, and finite automata models rather than standard arithmetic.
- **Trigger-Driven Logic:** Operations occur by routing active state triggers through the memory array logic elements.

## Instruction Formats & Opcodes
- `STATE_IN Rd, addr` — Load Input State: Opcode `0x01`. Activates parallel input transition state.
- `STATE_ADD Rd, Rs1, Rs2` — State Transition Add: Opcode `0x02`. Computes transition states.
- `STATE_OUT Rd, addr` — Store Output State: Opcode `0x03`. Stores transition output states to memory.
- `HLT` — Halt: Opcode `0x3F`.

## Assembly Stress Program (Vector Addition)
```assembly
# Micron Automata Vector Addition
STATE_IN R0, valA0
STATE_IN R1, valB0
STATE_ADD R0, R0, R1
STATE_OUT R0, valC0
HALT
```
