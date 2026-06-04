# MIPS I Fibonacci Example (Straight-line)
# Computes F(2) through F(6) starting with F(0)=0 and F(1)=1

# Load F(0) and F(1)
LW R1, valF0
LW R2, valF1

# F(2) = F(1) + F(0) = 1 + 0 = 1
ADDU R3, R2, R1
SW R3, valF2

# F(3) = F(2) + F(1) = 1 + 1 = 2
ADDU R4, R3, R2
SW R4, valF3

# F(4) = F(3) + F(2) = 2 + 1 = 3
ADDU R5, R4, R3
SW R5, valF4

# F(5) = F(4) + F(3) = 3 + 2 = 5
ADDU R6, R5, R4
SW R6, valF5

# F(6) = F(5) + F(4) = 5 + 3 = 8
ADDU R7, R6, R5
SW R7, valF6

HALT

ORG 64
valF0: DATA 0
valF1: DATA 1
valF2: DATA 0
valF3: DATA 0
valF4: DATA 0
valF5: DATA 0
valF6: DATA 0
