# NVIDIA GeForce 256 Vector Addition Benchmark

LD R1, valOne
LD R3, valOne

LD R0, valA0
LD R2, valB0
COMBINE R0, R0, R1, R2, R3
ST R0, valC0

LD R0, valA1
LD R2, valB1
COMBINE R0, R0, R1, R2, R3
ST R0, valC1

LD R0, valA2
LD R2, valB2
COMBINE R0, R0, R1, R2, R3
ST R0, valC2

LD R0, valA3
LD R2, valB3
COMBINE R0, R0, R1, R2, R3
ST R0, valC3

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

valOne: DATA 1
