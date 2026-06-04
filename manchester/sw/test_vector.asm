# Manchester Baby Vector Addition Benchmark

# Element 0: C0 = A0 + B0 -> LDN B0; SUB A0; STO C0; LDN C0; STO C0
LDN valB0
SUB valA0
STO valC0
LDN valC0
STO valC0

# Element 1: C1 = A1 + B1 -> LDN B1; SUB A1; STO C1; LDN C1; STO C1
LDN valB1
SUB valA1
STO valC1
LDN valC1
STO valC1

# Element 2: C2 = A2 + B2 -> LDN B2; SUB A2; STO C2; LDN C2; STO C2
LDN valB2
SUB valA2
STO valC2
LDN valC2
STO valC2

# Element 3: C3 = A3 + B3 -> LDN B3; SUB A3; STO C3; LDN C3; STO C3
LDN valB3
SUB valA3
STO valC3
LDN valC3
STO valC3

STP

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
