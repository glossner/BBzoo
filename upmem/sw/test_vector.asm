# UPMEM Vector Addition Benchmark
LW R1, R0, valA0
LW R2, R0, valB0
ADD R3, R1, R2
SW R3, R0, valC0

LW R1, R0, valA1
LW R2, R0, valB1
ADD R3, R1, R2
SW R3, R0, valC1

LW R1, R0, valA2
LW R2, R0, valB2
ADD R3, R1, R2
SW R3, R0, valC2

LW R1, R0, valA3
LW R2, R0, valB3
ADD R3, R1, R2
SW R3, R0, valC3

HALT

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
