# Cray-1 Vector Addition Test Program
A1 = 2
A2 = 16
A3 = 24
VL = A1
V0 = mem[A2]
V1 = V0 + V0
mem[A3] = V1
HLT

ORG 8
DATA 0,0,0,0,0,0,0,0

# Vector Data at address 16
DATA 5
DATA 15

ORG 18
DATA 0,0,0,0,0,0

# Target Store location at address 24
DATA 0
DATA 0
