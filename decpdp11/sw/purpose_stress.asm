MOV #valX, R0
MOV #valA, R1
MOV #valB, R2
MOV (R0), R3
ADD (R1), R3
SUB (R2), R3
MOV #valY, R4
MOV R3, (R4)
HALT
ORG 20
valX: DATA 10
valA: DATA 20
valB: DATA 5
valY: DATA 0
