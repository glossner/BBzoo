# IBM Stretch Vector Addition Benchmark

LDX X0, 0
LD ACC, valA0(X0)
ADD ACC, valB0(X0)
ST ACC, valC0(X0)

LDX X0, 1
LD ACC, valA0(X0)
ADD ACC, valB0(X0)
ST ACC, valC0(X0)

LDX X0, 2
LD ACC, valA0(X0)
ADD ACC, valB0(X0)
ST ACC, valC0(X0)

LDX X0, 3
LD ACC, valA0(X0)
ADD ACC, valB0(X0)
ST ACC, valC0(X0)

HLT

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
