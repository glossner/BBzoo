# IBM MFAST Vector Addition Benchmark (VLIW)
ORG 0
LD_CU R1, A0
LD_CU R2, B0
LD_CU R3, C0

# Load Vector A elements in parallel
NOP ; NOP ; LD R0, R1
# Load Vector B elements in parallel
NOP ; NOP ; LD R1, R2

# Add R0 and R1 to R2
ADD R2, R0, R1 ; NOP ; NOP
# Store R2 back to memory at C0
NOP ; NOP ; ST R2, R3

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
