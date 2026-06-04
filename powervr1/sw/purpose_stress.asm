LD R3, valDepth
LD R0, valX
LD R1, valY
HSR R0, R0, R1, R3
ST R0, valZ
ORG 20
valX: DATA 10
valY: DATA 20
valDepth: DATA 1
valZ: DATA 0
