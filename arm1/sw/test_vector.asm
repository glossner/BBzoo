# ARM1 Vector Addition Benchmark

# Element 0
LDR R1, valA0
LDR R2, valB0
ADD R1, R1, R2
STR R1, valC0

# Element 1
LDR R1, valA1
LDR R2, valB1
ADD R1, R1, R2
STR R1, valC1

# Element 2
LDR R1, valA2
LDR R2, valB2
ADD R1, R1, R2
STR R1, valC2

# Element 3
LDR R1, valA3
LDR R2, valB3
ADD R1, R1, R2
STR R1, valC3

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
