# SPARC Vector Addition Benchmark

# Element 0
LD [valA0], %g0
LD [valB0], %g1
ADD %g0, %g1, %g0
ST %g0, [valC0]

# Element 1
LD [valA1], %g0
LD [valB1], %g1
ADD %g0, %g1, %g0
ST %g0, [valC1]

# Element 2
LD [valA2], %g0
LD [valB2], %g1
ADD %g0, %g1, %g0
ST %g0, [valC2]

# Element 3
LD [valA3], %g0
LD [valB3], %g1
ADD %g0, %g1, %g0
ST %g0, [valC3]

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
