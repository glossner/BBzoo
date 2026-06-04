# Motorola 10800 Vector Addition Benchmark
ORG 0

loop:
# 1. Load element A
LD ACC, ptrA
LDI DR, ACC
ST DR, valA

# 2. Load element B and add
LD ACC, ptrB
LDI DR, ACC
LD ACC, valA
ADD ACC, ACC, DR
ST ACC, valC

# 3. Store result to C
LD ACC, ptrC
LD DR, valC
STI DR, ACC

# 4. Increment ptrA
LD ACC, ptrA
LD DR, one
ADD ACC, ACC, DR
ST ACC, ptrA

# 5. Increment ptrB
LD ACC, ptrB
LD DR, one
ADD ACC, ACC, DR
ST ACC, ptrB

# 6. Increment ptrC
LD ACC, ptrC
LD DR, one
ADD ACC, ACC, DR
ST ACC, ptrC

# 7. Decrement cnt and branch
LD ACC, cnt
LD DR, minus_one
ADD ACC, ACC, DR
ST ACC, cnt
JNZ ACC, loop
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
