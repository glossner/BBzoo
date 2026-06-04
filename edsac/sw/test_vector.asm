# EDSAC Vector Addition Benchmark

# Element 0: C0 = A0 + B0
T dummy
A valA0
A valB0
U valC0

# Element 1: C1 = A1 + B1
T dummy
A valA1
A valB1
U valC1

# Element 2: C2 = A2 + B2
T dummy
A valA2
A valB2
U valC2

# Element 3: C3 = A3 + B3
T dummy
A valA3
A valB3
U valC3

Z

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

dummy: DATA 0
