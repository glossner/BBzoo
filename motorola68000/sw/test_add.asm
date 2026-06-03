# Motorola 68000 Simple Addition Test Program
MOVEA.L #0x1000, A0
MOVE.L #15, D0
MOVE.L #27, D1
ADD.L D1, D0
MOVE.L D0, (A0)
loop: BRA loop
