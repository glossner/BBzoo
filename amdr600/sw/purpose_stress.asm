NOP | LD R0, valX
NOP | LD R1, valY
ADD R0, R0, R1 | NOP
NOP | ST R0, valZ
ORG 20
valX: DATA 10
valY: DATA 20
valZ: DATA 0
