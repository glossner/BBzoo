LD R1, valOne
LD R3, valOne
LD R0, valX
LD R2, valY
COMBINE R0, R0, R1, R2, R3
ST R0, valZ
ORG 20
valX: DATA 10
valY: DATA 20
valOne: DATA 1
valZ: DATA 0
