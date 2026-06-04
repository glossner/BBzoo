# MOS 6502 Factorial of 3 Example (Straight-line)
# 3! = 3 + 3 = 6

CLC
LDA val3
ADC val3
STA result

BRK

ORG 32
val3: DATA 3
result: DATA 0
