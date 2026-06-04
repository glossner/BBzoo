# 3dfx Voodoo1 Vector Addition Benchmark

LD R0, valA0
LD R1, valB0
ADD R0, R0, R1
ST R0, valC0

LD R0, valA1
LD R1, valB1
ADD R0, R0, R1
ST R0, valC1

LD R0, valA2
LD R1, valB2
ADD R0, R0, R1
ST R0, valC2

LD R0, valA3
LD R1, valB3
ADD R0, R0, R1
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
