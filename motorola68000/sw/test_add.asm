# Motorola 68000 Vector Addition Benchmark (Unrolled)

# Element 0
MOVEA.L #valA0, A0
MOVE.L (A0), D0
MOVEA.L #valB0, A1
MOVE.L (A1), D1
ADD.L D1, D0
MOVEA.L #valC0, A2
MOVE.L D0, (A2)

# Element 1
MOVEA.L #valA1, A0
MOVE.L (A0), D0
MOVEA.L #valB1, A1
MOVE.L (A1), D1
ADD.L D1, D0
MOVEA.L #valC1, A2
MOVE.L D0, (A2)

# Element 2
MOVEA.L #valA2, A0
MOVE.L (A0), D0
MOVEA.L #valB2, A1
MOVE.L (A1), D1
ADD.L D1, D0
MOVEA.L #valC2, A2
MOVE.L D0, (A2)

# Element 3
MOVEA.L #valA3, A0
MOVE.L (A0), D0
MOVEA.L #valB3, A1
MOVE.L (A1), D1
ADD.L D1, D0
MOVEA.L #valC3, A2
MOVE.L D0, (A2)

# Unconditional loop (halt detection target)
loop: BRA loop

ORG 68

# Vector A
valA0: DATA 0, 10
valA1: DATA 0, 20
valA2: DATA 0, 30
valA3: DATA 0, 40

# Vector B
valB0: DATA 0, 1
valB1: DATA 0, 2
valB2: DATA 0, 3
valB3: DATA 0, 4

# Vector C
valC0: DATA 0, 0
valC1: DATA 0, 0
valC2: DATA 0, 0
valC3: DATA 0, 0
