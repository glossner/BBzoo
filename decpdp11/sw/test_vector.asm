# DEC PDP-11 Vector Addition Benchmark

MOV #valA0, R0
MOV #valB0, R1
MOV #valC0, R2

# Element 0
MOV (R0)+, R3
ADD (R1)+, R3
MOV R3, (R2)+

# Element 1
MOV (R0)+, R3
ADD (R1)+, R3
MOV R3, (R2)+

# Element 2
MOV (R0)+, R3
ADD (R1)+, R3
MOV R3, (R2)+

# Element 3
MOV (R0)+, R3
ADD (R1)+, R3
MOV R3, (R2)+

HALT

ORG 20
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
