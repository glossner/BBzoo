# Architecture Comparison Report

| Target Architecture | Word Width (bits) | Execution Cycles | Retired Instructions | Memory Reads | Memory Writes | CPI | Code Footprint (words) | ALU Duty Cycle | Mem BW Efficiency | Register Port Stress |
|---------------------|-------------------|------------------|----------------------|--------------|---------------|-----|------------------------|----------------|-------------------|----------------------|
| Babbage Anal. Eng.  | 64                | 38              | 13                   | 21            | 4             | 2.92 | 32 | 10.5% | 15.38 B/inst | 0.5 regs/inst |
| Harvard Mark I      | 64                | 26              | 9                   | 9            | 0             | 2.89 | 9 | 15.4% | 8.00 B/inst | 0.5 regs/inst |
| Zuse Z1             | 22                | 54              | 21                   | 29            | 4             | 2.57 | 52 | 7.4% | 4.32 B/inst | 0.5 regs/inst |
| Manchester Baby     | 32                | 62              | 21                   | 33            | 8             | 2.95 | 52 | 6.5% | 7.81 B/inst | 0.5 regs/inst |
| Univac I            | 72                | 38              | 13                   | 21            | 4             | 2.92 | 52 | 10.5% | 17.31 B/inst | 0.5 regs/inst |
| Princeton IAS       | 40                | 38              | 13                   | 21            | 4             | 2.92 | 52 | 10.5% | 9.62 B/inst | 0.5 regs/inst |
| EDSAC               | 17                | 50              | 17                   | 25            | 8             | 2.94 | 53 | 8.0% | 4.13 B/inst | 0.5 regs/inst |
| IBM 701             | 36                | 38              | 13                   | 21            | 4             | 2.92 | 52 | 10.5% | 8.65 B/inst | 1.0 regs/inst |
| IBM 704             | 36                | 38              | 13                   | 21            | 4             | 2.92 | 52 | 10.5% | 8.65 B/inst | 1.0 regs/inst |
| IBM 650             | 40                | 38              | 13                   | 21            | 4             | 2.92 | 52 | 10.5% | 9.62 B/inst | 1.0 regs/inst |
| IBM 705             | 35                | 38              | 13                   | 21            | 4             | 2.92 | 52 | 10.5% | 8.41 B/inst | 1.0 regs/inst |
| IBM 1401            | 36                | 38              | 9                   | 21            | 8             | 4.22 | 52 | 10.5% | 14.50 B/inst | 1.0 regs/inst |
| STC ZEBRA           | 33                | 38              | 13                   | 21            | 4             | 2.92 | 52 | 10.5% | 7.93 B/inst | 1.0 regs/inst |
| Bull Gamma 60        | 24                | 45              | 16                   | 24            | 4             | 2.81 | 52 | 8.9% | 5.25 B/inst | 1.0 regs/inst |
| IBM Stretch         | 64                | 46              | 17                   | 25            | 4             | 2.71 | 52 | 8.7% | 13.65 B/inst | 1.0 regs/inst |
| MOS 6502            | 8                 | 70              | 17                   | 49            | 4             | 4.12 | 72 | 5.7% | 3.12 B/inst | 1.2 regs/inst |
| DEC PDP-8           | 12                | 133              | 33                   | 69            | 20             | 4.03 | 26 | 3.0% | 4.05 B/inst | 1.2 regs/inst |
| DEC PDP-11          | 16                | 81              | 16                   | 31            | 4             | 5.06 | 32 | 4.9% | 4.38 B/inst | 2.5 regs/inst |
| IBM System/360      | 32                | 56              | 12                   | 20            | 4             | 4.67 | 100 | 7.1% | 8.00 B/inst | 1.0 regs/inst |
| Motorola 68000      | 32                | 157              | 28                   | 68            | 8             | 5.61 | 92 | 2.5% | 10.86 B/inst | 2.5 regs/inst |
| Burroughs B5500     | 48                | 46              | 17                   | 25            | 4             | 2.71 | 32 | 13.0% | 10.24 B/inst | 0.5 regs/inst |
| CDC 6600            | 60                | 46              | 17                   | 25            | 4             | 2.71 | 32 | 8.7% | 12.79 B/inst | 1.0 regs/inst |
| Cray-1              | 64 (Vector)       | 40              | 10                   | 18            | 4             | 4.00 | 28 | 10.0% | 17.60 B/inst | 4.5 regs/inst |
| Univac 1103A        | 36                | 38              | 9                   | 21            | 8             | 4.22 | 52 | 10.5% | 14.50 B/inst | 1.0 regs/inst |
| CDC 6600 PPU        | 12                | 38              | 13                   | 21            | 4             | 2.92 | 52 | 10.5% | 2.88 B/inst | 1.0 regs/inst |
| DEC VAX             | 32                | 81              | 16                   | 31            | 4             | 5.06 | 32 | 4.9% | 8.75 B/inst | 2.5 regs/inst |
| Intel 8080A         | 8                 | 78              | 21                   | 53            | 4             | 3.71 | 92 | 5.1% | 2.71 B/inst | 1.2 regs/inst |
| Motorola 6800       | 8                 | 62              | 13                   | 45            | 4             | 4.77 | 92 | 6.5% | 3.77 B/inst | 1.2 regs/inst |
| IBM 6150 ROMP       | 32                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 13.8% | 9.65 B/inst | 2.5 regs/inst |
| MIPS I (R2000)      | 32                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 13.8% | 9.65 B/inst | 2.5 regs/inst |
| ARM1                | 32                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 13.8% | 9.65 B/inst | 2.5 regs/inst |
| Berkeley RISC-I     | 32                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 13.8% | 9.65 B/inst | 2.5 regs/inst |
| HP 3000             | 16                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 13.8% | 4.82 B/inst | 0.5 regs/inst |
| Ethlilith           | 16                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 13.8% | 4.82 B/inst | 0.5 regs/inst |
| UCSD Pascal P-Mach  | 16                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 10.3% | 4.82 B/inst | 0.5 regs/inst |
| NEC uPD7720 DSP     | 16                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 6.9% | 4.82 B/inst | 1.2 regs/inst |
| TI TMS32010 DSP     | 16                | 50              | 13                   | 33            | 4             | 3.85 | 52 | 8.0% | 5.69 B/inst | 1.2 regs/inst |
| ADI ADSP-2100 DSP   | 16                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 6.9% | 4.82 B/inst | 1.2 regs/inst |
| IBM MWave DSP       | 16                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 6.9% | 4.82 B/inst | 1.2 regs/inst |
| 3dfx Voodoo1        | 32                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 6.9% | 9.65 B/inst | 4.5 regs/inst |
| NVIDIA GeForce 256  | 32                | 66              | 19                   | 43            | 4             | 3.47 | 53 | 6.1% | 9.89 B/inst | 4.5 regs/inst |
| ATI Radeon R100     | 32                | 62              | 18                   | 40            | 4             | 3.44 | 53 | 6.5% | 9.78 B/inst | 4.5 regs/inst |
| PowerVR Series 1    | 32                | 62              | 18                   | 40            | 4             | 3.44 | 53 | 6.5% | 9.78 B/inst | 4.5 regs/inst |
| ARM Mali-200 GPU    | 32                | 25              | 5                   | 16            | 4             | 5.00 | 52 | 4.0% | 16.00 B/inst | 4.5 regs/inst |
| AMD R600 GPU        | 32                | 38              | 13                   | 21            | 4             | 2.92 | 52 | 10.5% | 7.69 B/inst | 4.5 regs/inst |
