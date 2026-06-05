# MIT Dataflow Vector Addition Benchmark

# Element 0
ND_LOAD 4, 0, valA0
ND_LOAD 4, 1, valB0
ND_ADD 5
ND_STORE 0, valC0

# Element 1
ND_LOAD 11, 0, valA1
ND_LOAD 11, 1, valB1
ND_ADD 12
ND_STORE 0, valC1

# Element 2
ND_LOAD 18, 0, valA2
ND_LOAD 18, 1, valB2
ND_ADD 19
ND_STORE 0, valC2

# Element 3
ND_LOAD 25, 0, valA3
ND_LOAD 25, 1, valB3
ND_ADD 26
ND_STORE 0, valC3

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
