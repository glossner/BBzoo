# NEC uPD7720 Vector Addition Benchmark

# Element 0
LD A, valA0
LD B, valB0
ADD
ST A, valC0

# Element 1
LD A, valA1
LD B, valB1
ADD
ST A, valC1

# Element 2
LD A, valA2
LD B, valB2
ADD
ST A, valC2

# Element 3
LD A, valA3
LD B, valB3
ADD
ST A, valC3

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
