# SUBLEQ Optimized Vector Addition Benchmark

# Element 0 (Uses initial temp=0, valC0=0)
SUBLEQ valA0, temp, step2
step2: SUBLEQ temp, valC0, step4
step4: SUBLEQ temp, temp, step5
step5: SUBLEQ valB0, temp, step6
step6: SUBLEQ temp, valC0, step7

# Element 1
step7: SUBLEQ temp, temp, step8
step8: SUBLEQ valA1, temp, step10
step10: SUBLEQ temp, valC1, step11
step11: SUBLEQ temp, temp, step12
step12: SUBLEQ valB1, temp, step13
step13: SUBLEQ temp, valC1, jump_after_data

# Jump past data section (which is at 40-52)
jump_after_data: SUBLEQ temp, temp, step14

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

temp: DATA 0

ORG 53
# Element 2
step14: SUBLEQ temp, temp, step15
step15: SUBLEQ valA2, temp, step17
step17: SUBLEQ temp, valC2, step18
step18: SUBLEQ temp, temp, step19
step19: SUBLEQ valB2, temp, step20
step20: SUBLEQ temp, valC2, step21

# Element 3
step21: SUBLEQ temp, temp, step22
step22: SUBLEQ valA3, temp, step24
step24: SUBLEQ temp, valC3, step25
step25: SUBLEQ temp, temp, step26
step26: SUBLEQ valB3, temp, step27
step27: SUBLEQ temp, valC3, end_lbl

end_lbl: HALT
