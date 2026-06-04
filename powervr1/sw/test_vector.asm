# PowerVR Series 1 Vector Addition Benchmark

LD R3, valDepth

LD R0, valA0
LD R1, valB0
HSR R0, R0, R1, R3
ST R0, valC0

LD R0, valA1
LD R1, valB1
HSR R0, R0, R1, R3
ST R0, valC1

LD R0, valA2
LD R1, valB2
HSR R0, R0, R1, R3
ST R0, valC2

LD R0, valA3
LD R1, valB3
HSR R0, R0, R1, R3
ST R0, valC3

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

valDepth: DATA 50
