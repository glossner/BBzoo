# Harvard Mark I Vector Addition Benchmark
# Inputs: R20-R23 (A), R24-R27 (B)
# Outputs: R28-R31 (C)

MOV R20, R28
ADD R24, R28

MOV R21, R29
ADD R25, R29

MOV R22, R30
ADD R26, R30

MOV R23, R31
ADD R27, R31

HLT
