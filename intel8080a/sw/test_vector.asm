# Intel 8080A Vector Addition Benchmark

# Element 0
LDA valA0
MOV B, A
LDA valB0
ADD B
STA valC0

# Element 1
LDA valA1
MOV B, A
LDA valB1
ADD B
STA valC1

# Element 2
LDA valA2
MOV B, A
LDA valB2
ADD B
STA valC2

# Element 3
LDA valA3
MOV B, A
LDA valB3
ADD B
STA valC3

HLT

ORG 80
valA0: DATA 10
valA1: DATA 20
valA2: DATA 30
valA3: DATA 40

valB0: DATA 1
valB1: DATA 2
valB2: DATA 3
valB3: DATA 4

valC0: DATA 0
valC1: DATA 0
valC2: DATA 0
valC3: DATA 0
