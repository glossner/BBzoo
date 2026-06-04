# MOS 6502 Vector Addition Benchmark

# Element 0
LDA valA0
CLC
ADC valB0
STA valC0

# Element 1
LDA valA1
CLC
ADC valB1
STA valC1

# Element 2
LDA valA2
CLC
ADC valB2
STA valC2

# Element 3
LDA valA3
CLC
ADC valB3
STA valC3

BRK

ORG 60
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
