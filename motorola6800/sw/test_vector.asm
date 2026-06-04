# Motorola 6800 Vector Addition Benchmark

# Element 0
LDAA valA0
ADDA valB0
STAA valC0

# Element 1
LDAA valA1
ADDA valB1
STAA valC1

# Element 2
LDAA valA2
ADDA valB2
STAA valC2

# Element 3
LDAA valA3
ADDA valB3
STAA valC3

WAI

ORG 80
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
