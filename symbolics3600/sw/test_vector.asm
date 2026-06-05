# Symbolics 3600 Vector Addition Benchmark

# Element 0
ILOAD valA0
ILOAD valB0
IADD
ISTORE valC0

# Element 1
ILOAD valA1
ILOAD valB1
IADD
ISTORE valC1

# Element 2
ILOAD valA2
ILOAD valB2
IADD
ISTORE valC2

# Element 3
ILOAD valA3
ILOAD valB3
IADD
ISTORE valC3

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
