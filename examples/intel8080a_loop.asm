# Intel 8080A Double Value Example
# Loads a value, moves it to register B, and adds it to itself to double it

LDA valX
MOV B, A
ADD B
STA valY

HLT

ORG 32
valX: DATA 15
valY: DATA 0
