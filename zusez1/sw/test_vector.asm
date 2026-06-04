# Zuse Z1 Vector Addition Benchmark

# Element 0
PR valA0
MOV R1, R2
PR valB0
ADD
PS valC0

# Element 1
PR valA1
MOV R1, R2
PR valB1
ADD
PS valC1

# Element 2
PR valA2
MOV R1, R2
PR valB2
ADD
PS valC2

# Element 3
PR valA3
MOV R1, R2
PR valB3
ADD
PS valC3

HLT

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
