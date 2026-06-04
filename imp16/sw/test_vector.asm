# National Semiconductor IMP-16 Vector Addition Benchmark
ORG 0

loop:
# 1. Load element A
LD R0, ptrA
LDI R1, R0
ST R1, valA

# 2. Load element B and add
LD R0, ptrB
LDI R1, R0
LD R2, valA
ADD R1, R1, R2
ST R1, valC

# 3. Store result to C
LD R0, ptrC
LD R1, valC
STI R1, R0

# 4. Increment ptrA
LD R0, ptrA
LD R1, one
ADD R0, R0, R1
ST R0, ptrA

# 5. Increment ptrB
LD R0, ptrB
LD R1, one
ADD R0, R0, R1
ST R0, ptrB

# 6. Increment ptrC
LD R0, ptrC
LD R1, one
ADD R0, R0, R1
ST R0, ptrC

# 7. Decrement cnt and branch
LD R0, cnt
LD R1, minus_one
ADD R0, R0, R1
ST R0, cnt
JNZ R0, loop
HLT

# Variables and pointers
ptrA: DATA A0
ptrB: DATA B0
ptrC: DATA C0
cnt: DATA 4
one: DATA 1
minus_one: DATA -1
valA: DATA 0
valC: DATA 0

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
