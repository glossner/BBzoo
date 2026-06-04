# AMD Am2901 Vector Addition Benchmark
ORG 0
# Initial load of pointers and counter
LD R0, ptrA
LD R1, ptrB
LD R2, ptrC
LD R3, cnt
LD R4, one
LD R5, minus_one

loop:
# Load elements indirectly
LDI R8, R0
LDI R9, R1
# Add them
ADD R10, R8, R9
# Store element indirectly
STI R10, R2

# Increment pointers
ADD R0, R0, R4
ADD R1, R1, R4
ADD R2, R2, R4

# Decrement counter
ADD R3, R3, R5

# Jump if R3 not zero
JNZ R3, loop
HLT

# Variables and pointers
ptrA: DATA A0
ptrB: DATA B0
ptrC: DATA C0
cnt: DATA 4
one: DATA 1
minus_one: DATA -1

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
