# Cydrome Cydra 5 Vector Addition Benchmark using Rotating Registers
ORG 0
LD_CU R1, A0
LD_CU R2, B0
LD_CU R3, C0
LD_CU R0, ONE
NOP ; LD R0, R0 ; NOP

# Element 0
NOP ; LD R4, R1 ; NOP
NOP ; LD R4, R2 ; BR_ROT loop1
loop1:
ADD R6, R5, R4 ; ST R6, R3 ; NOP
ADD R1, R1, R0 ; NOP ; NOP
ADD R2, R2, R0 ; NOP ; NOP
ADD R3, R3, R0 ; NOP ; NOP

# Element 1
NOP ; LD R4, R1 ; NOP
NOP ; LD R4, R2 ; BR_ROT loop2
loop2:
ADD R6, R5, R4 ; ST R6, R3 ; NOP
ADD R1, R1, R0 ; NOP ; NOP
ADD R2, R2, R0 ; NOP ; NOP
ADD R3, R3, R0 ; NOP ; NOP

# Element 2
NOP ; LD R4, R1 ; NOP
NOP ; LD R4, R2 ; BR_ROT loop3
loop3:
ADD R6, R5, R4 ; ST R6, R3 ; NOP
ADD R1, R1, R0 ; NOP ; NOP
ADD R2, R2, R0 ; NOP ; NOP
ADD R3, R3, R0 ; NOP ; NOP

# Element 3
NOP ; LD R4, R1 ; NOP
NOP ; LD R4, R2 ; BR_ROT loop4
loop4:
ADD R6, R5, R4 ; ST R6, R3 ; NOP

HLT

# Constants & Vector Data
ORG 80
ONE: DATA 1

ORG 84
A0: DATA 10
A1: DATA 20
A2: DATA 30
A3: DATA 40

ORG 88
B0: DATA 1
B1: DATA 2
B2: DATA 3
B3: DATA 4

ORG 92
C0: DATA 0
C1: DATA 0
C2: DATA 0
C3: DATA 0
