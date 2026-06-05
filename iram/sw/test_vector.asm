# IRAM Vector Addition Benchmark
LW R10, addrA
LW R11, addrB
LW R12, addrC

VLD V1, R10
VLD V2, R11
VADD.W V3, V1, V2
VST V3, R12
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

addrA: DATA 40
addrB: DATA 44
addrC: DATA 48
