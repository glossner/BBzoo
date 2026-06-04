# ARM Mali-200 Vector Addition Benchmark

VLD V0, valA0
VLD V1, valB0
VADD V2, V0, V1
VST V2, valC0
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
