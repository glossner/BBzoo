# Samsung HBM2-PIM Vector Addition Benchmark
PCU_LD V1, valA0
PCU_LD V2, valB0
PCU_ADD V3, V1, V2
PCU_ST V3, valC0
HALT

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
