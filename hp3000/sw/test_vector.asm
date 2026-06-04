# HP 3000 Vector Addition Benchmark

PUSH valA0
PUSH valB0
ADD
POP valC0

PUSH valA1
PUSH valB1
ADD
POP valC1

PUSH valA2
PUSH valB2
ADD
POP valC2

PUSH valA3
PUSH valB3
ADD
POP valC3

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
