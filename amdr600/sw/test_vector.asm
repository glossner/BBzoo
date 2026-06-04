# AMD R600 VLIW Vector Addition Benchmark

NOP | LD R0, valA0
NOP | LD R1, valB0
ADD R0, R0, R1 | LD R2, valA1
NOP | ST R0, valC0
NOP | LD R3, valB1
ADD R2, R2, R3 | LD R0, valA2
NOP | ST R2, valC1
NOP | LD R1, valB2
ADD R0, R0, R1 | LD R2, valA3
NOP | ST R0, valC2
NOP | LD R3, valB3
ADD R2, R2, R3 | NOP
HALT | ST R2, valC3

ORG 40
valA0: DATA 10
valA1: DATA 20
valA2: DATA 30
valA3: DATA 40

valB0: DATA 1
valB1: DATA 2
valB2: DATA 3
valB3: DATA 4

valC0: DATA 0
valC1: DATA 0
valC2: DATA 0
valC3: DATA 0
