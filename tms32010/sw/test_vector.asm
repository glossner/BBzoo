# TI TMS32010 Vector Addition Benchmark

# Element 0
LAC valA0
ADD valB0
SACL valC0

# Element 1
LAC valA1
ADD valB1
SACL valC1

# Element 2
LAC valA2
ADD valB2
SACL valC2

# Element 3
LAC valA3
ADD valB3
SACL valC3

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
