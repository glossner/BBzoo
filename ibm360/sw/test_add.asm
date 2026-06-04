# IBM System/360 Vector Addition Benchmark (Unrolled)

# Element 0
L 1, valA0
A 1, valB0
ST 1, valC0

# Element 1
L 1, valA1
A 1, valB1
ST 1, valC1

# Element 2
L 1, valA2
A 1, valB2
ST 1, valC2

# Element 3
L 1, valA3
A 1, valB3
ST 1, valC3

# Infinite loop (halt condition)
loop: BC 15, loop

ORG 52

# Vector A
valA0: DATA 0,0,0,10
valA1: DATA 0,0,0,20
valA2: DATA 0,0,0,30
valA3: DATA 0,0,0,40

# Vector B
valB0: DATA 0,0,0,1
valB1: DATA 0,0,0,2
valB2: DATA 0,0,0,3
valB3: DATA 0,0,0,4

# Vector C (Result)
valC0: DATA 0,0,0,0
valC1: DATA 0,0,0,0
valC2: DATA 0,0,0,0
valC3: DATA 0,0,0,0
