# IBM System/360 Simple Addition Test Program
L 1, val1
L 2, val2
AR 1, 2
ST 1, result
loop: BC 15, loop

ORG 18
DATA 0,0,0,0,0,0,0,0,0,0,0,0,0,0

val1: DATA 0,0,0,42
val2: DATA 0,0,0,24
result: DATA 0,0,0,0
