# Cray-1 Vector Addition Benchmark
A1 = 4
A2 = 16
A3 = 20
A4 = 24
VL = A1
V0 = mem[A2]
V1 = mem[A3]
V2 = V0 + V1
mem[A4] = V2
HLT

ORG 16
# Vector A data at address 16
DATA 10
DATA 20
DATA 30
DATA 40

# Vector B data at address 20
DATA 1
DATA 2
DATA 3
DATA 4

# Vector C data at address 24
DATA 0
DATA 0
DATA 0
DATA 0
