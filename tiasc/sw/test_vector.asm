# TI ASC Vector Addition Benchmark
ORG 0
LD_CU R1, A0
LD_CU R2, B0
LD_CU R3, C0
LD_CU R4, 4
VADD_ASC R3, R1, R2, R4
HLT

# Constants & Vector Data
ORG 80
VLEN: DATA 4

ORG 84
A0: DATA 10, 20, 30, 40

ORG 88
B0: DATA 1, 2, 3, 4

ORG 92
C0: DATA 0, 0, 0, 0
