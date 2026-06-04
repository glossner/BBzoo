# DEC VAX Vector Addition Benchmark

MOVL #valA0, R0
MOVL #valB0, R1
MOVL #valC0, R2

# Element 0
MOVL (R0)+, R3
ADDL2 (R1)+, R3
MOVL R3, (R2)+

# Element 1
MOVL (R0)+, R3
ADDL2 (R1)+, R3
MOVL R3, (R2)+

# Element 2
MOVL (R0)+, R3
ADDL2 (R1)+, R3
MOVL R3, (R2)+

# Element 3
MOVL (R0)+, R3
ADDL2 (R1)+, R3
MOVL R3, (R2)+

HALT

ORG 20
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
