# Bull Gamma 60 Vector Addition Benchmark

FORK thread1_start
LD acc0, valA0
ADD acc0, valB0
ST acc0, valC0
LD acc0, valA1
ADD acc0, valB1
ST acc0, valC1
JOIN
HLT

thread1_start:
LD acc1, valA2
ADD acc1, valB2
ST acc1, valC2
LD acc1, valA3
ADD acc1, valB3
ST acc1, valC3
JOIN

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
