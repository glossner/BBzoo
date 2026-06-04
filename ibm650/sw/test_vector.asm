# IBM 650 Vector Addition Benchmark

LD valA0, inst1
inst1: ADD valB0, inst2
inst2: ST valC0, inst3

inst3: LD valA1, inst4
inst4: ADD valB1, inst5
inst5: ST valC1, inst6

inst6: LD valA2, inst7
inst7: ADD valB2, inst8
inst8: ST valC2, inst9

inst9: LD valA3, inst10
inst10: ADD valB3, inst11
inst11: ST valC3, halt_inst

halt_inst: HLT

ORG 40
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
