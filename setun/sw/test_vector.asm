# Setun Vector Addition Benchmark

# Element 0
LOAD valA0
LOAD valB0
ADD
STORE valC0

# Element 1
LOAD valA1
LOAD valB1
ADD
STORE valC1

# Element 2
LOAD valA2
LOAD valB2
ADD
STORE valC2

# Element 3
LOAD valA3
LOAD valB3
ADD
STORE valC3

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
