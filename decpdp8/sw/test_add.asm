# DEC PDP-8 Vector Addition Benchmark
ORG 0
CLA CLL

# Loop start
loop:
TAD I ptrA
TAD I ptrB
DCA I ptrC

ISZ ptrA
ISZ ptrB
ISZ ptrC

ISZ cnt
JMP loop
HLT

# Variables and pointers
ptrA: DATA A0
ptrB: DATA B0
ptrC: DATA C0
cnt: DATA -4

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
