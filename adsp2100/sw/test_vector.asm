# ADI ADSP-2100 Vector Addition Benchmark

# Element 0
LD AX0, valA0
LD AY0, valB0
ADD AR
ST AR, valC0

# Element 1
LD AX0, valA1
LD AY0, valB1
ADD AR
ST AR, valC1

# Element 2
LD AX0, valA2
LD AY0, valB2
ADD AR
ST AR, valC2

# Element 3
LD AX0, valA3
LD AY0, valB3
ADD AR
ST AR, valC3

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
