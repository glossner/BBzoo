# CDC 6600 Vector Addition Benchmark

# Element 0
A1 = B0 + valA0
A2 = B0 + valB0
X6 = X1 + X2
A6 = B0 + valC0

# Element 1
A1 = B0 + valA1
A2 = B0 + valB1
X6 = X1 + X2
A6 = B0 + valC1

# Element 2
A1 = B0 + valA2
A2 = B0 + valB2
X6 = X1 + X2
A6 = B0 + valC2

# Element 3
A1 = B0 + valA3
A2 = B0 + valB3
X6 = X1 + X2
A6 = B0 + valC3

HLT

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
