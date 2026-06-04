# ILLIAC IV Vector Addition Benchmark
ORG 0
LD_CU R1, A0
LD_PE
LD_CU R1, B0
ADD_PE
LD_CU R1, C0
ST_PE
HLT

# Vector A data
A0: DATA 10
A1: DATA 20
A2: DATA 30
A3: DATA 40

# Vector B data
B0: DATA 1
B1: DATA 2
B2: DATA 3
B3: DATA 4

# Vector C data (destination)
C0: DATA 0
C1: DATA 0
C2: DATA 0
C3: DATA 0
