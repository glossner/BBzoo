# Intel Itanium EPIC Vector Addition Benchmark
ORG 0
{ .mmi
  ld_cu r1 = A0
  ld_cu r2 = B0
  ld_cu r3 = C0 ;;
}
{ .mmi
  ld_cu r7 = TWO
  nop 0
  nop 0 ;;
}
{ .mmi
  ld8 r7 = [r7]
  nop 0
  nop 0 ;;
}

# Element 0
{ .mmi
  ld8 r4 = [r1]
  ld8 r5 = [r2]
  nop 0 ;;
}
{ .mmi
  add r6 = r4, r5
  nop 0
  nop 0 ;;
}
{ .mmi
  st8 [r3] = r6
  nop 0
  nop 0 ;;
}

# Element 1
{ .mmi
  add r1 = r1, r7
  add r2 = r2, r7
  add r3 = r3, r7 ;;
}
{ .mmi
  ld8 r4 = [r1]
  ld8 r5 = [r2]
  nop 0 ;;
}
{ .mmi
  add r6 = r4, r5
  nop 0
  nop 0 ;;
}
{ .mmi
  st8 [r3] = r6
  nop 0
  nop 0 ;;
}

# Element 2
{ .mmi
  add r1 = r1, r7
  add r2 = r2, r7
  add r3 = r3, r7 ;;
}
{ .mmi
  ld8 r4 = [r1]
  ld8 r5 = [r2]
  nop 0 ;;
}
{ .mmi
  add r6 = r4, r5
  nop 0
  nop 0 ;;
}
{ .mmi
  st8 [r3] = r6
  nop 0
  nop 0 ;;
}

# Element 3
{ .mmi
  add r1 = r1, r7
  add r2 = r2, r7
  add r3 = r3, r7 ;;
}
{ .mmi
  ld8 r4 = [r1]
  ld8 r5 = [r2]
  nop 0 ;;
}
{ .mmi
  add r6 = r4, r5
  nop 0
  nop 0 ;;
}
{ .mmi
  st8 [r3] = r6
  nop 0
  nop 0 ;;
}

{ .mmi
  hlt
  nop 0
  nop 0 ;;
}

# Constants & Vector Data
ORG 80
TWO: DATA 2, 0

ORG 84
A0: DATA 10, 0
A1: DATA 20, 0
A2: DATA 30, 0
A3: DATA 40, 0

ORG 92
B0: DATA 1, 0
B1: DATA 2, 0
B2: DATA 3, 0
B3: DATA 4, 0

ORG 100
C0: DATA 0, 0
C1: DATA 0, 0
C2: DATA 0, 0
C3: DATA 0, 0
