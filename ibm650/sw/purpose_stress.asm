LD valX, inst1
inst1: ADD valA, inst2
inst2: ST valY, inst3
inst3: HLT
ORG 20
valX: DATA 10
valA: DATA 20
valY: DATA 0
