# BBZoo 🦁

Welcome to **BBZoo**, a simulation playground of historical computer architectures implemented in **Chisel / Scala**. The design is inspired by the classic textbook *"Computer Architecture: Concepts and Evolution"* by Gerrit A. Blaauw and Frederick P. Brooks, Jr. This repository implements **64 unique processor architectures** with fully synthesizable datapaths, synthetic Performance Monitoring Units (PMUs) to track cycles, instruction retirement, memory reads, and memory writes, as well as a unified 4-element Vector Addition benchmark.

I have always wanted to create a version of the Zoo ever since I was a student of Dr. Brooks in the early 1990's. There were rumors of executable APL code for the entire Zoo but I never located it. Technology has progressed so fast that this entire repository was vibe coded in 2 days using Google's Antigravity 2.0 IDE. I'm sure there are omissions, errors, and other artifacts. Given that it took me 30+ years for the first implementation, I wouldn't count on significant upgrades anytime soon. However, I will accept pull requests for bugs, capabilities, and new <u>*architectures*</u>. I have a strong preference only for architectures that have new features. I'm not that interested in microarchitectures. Not that they aren't super important (I mean seriously, you can't build a machine today without caches, branch prediction, superscalar, etc.) its just that this is meant to be an architecture repository.

Each machine directory has a brief overview of the architecture, its unique features, instruction formats, an assembler, and simple programs that execute on the machine. 

Cheers,
John Glossner
June, 2026 

---

## 🏛️ Architectural Houses

The architectures in the zoo are organized into 16 distinct "houses" based on their core datapath and register organization paradigms:

### 1. Pioneer House
*Characterized by the earliest explorations of stored-program, mechanical, or relay-based calculation.*
- ⚙️ **Babbage Analytical Engine** (Charles Babbage, 1837) — [babbage](babbage) / [arch.md](babbage/arch.md)
- 🎮 **Harvard Mark I** (Howard Aiken, 1944) — [harvardmark1](harvardmark1) / [arch.md](harvardmark1/arch.md)
- 💾 **Zuse Z1** (Konrad Zuse, 1938) — [zusez1](zusez1) / [arch.md](zusez1/arch.md)
- 👶 **Manchester MU1** (Frederic Williams & Tom Kilburn, 1948) — [manchestermu1](manchestermu1) / [arch.md](manchestermu1/arch.md)
- 📼 **Univac I** (J. Presper Eckert & John Mauchly, 1951) — [univac1](univac1) / [arch.md](univac1/arch.md)

### 2. Von Neumann House
*Featuring the classic accumulator-based stored-program organization with unified program and data memory.*
- 🏫 **Princeton IAS** (John von Neumann, 1952) — [princetonias](princetonias) / [arch.md](princetonias/arch.md)
- 📡 **EDSAC** (Maurice Wilkes, 1949) — [cambridgeedsac](cambridgeedsac) / [arch.md](cambridgeedsac/arch.md)
- 🔬 **IBM 701** (Nathaniel Rochester, 1952) — [ibm701](ibm701) / [arch.md](ibm701/arch.md)
- 📈 **IBM 704** (Gene Amdahl, 1954) — [ibm704](ibm704) / [arch.md](ibm704/arch.md)
- 🚂 **IBM 650** (Frank Hamilton, Ernest Hughes, & James Birkenstock, 1953) [replicated from IBM House] — [ibm650](ibm650) / [arch.md](ibm650/arch.md)
- 🏢 **IBM 705** (Werner Buchholz, 1954) [replicated from IBM House] — [ibm705](ibm705) / [arch.md](ibm705/arch.md)
- 📇 **IBM 1401** (Chuck Branscomb, 1959) [replicated from IBM House] — [ibm1401](ibm1401) / [arch.md](ibm1401/arch.md)
- 📉 **DEC PDP-8** (C. Gordon Bell & Edson de Castro, 1965) [replicated from Bell House] — [decpdp8](decpdp8) / [arch.md](decpdp8/arch.md)

### 3. IBM House
*Representing the commercial business and variable-word-length scientific computer evolution.*
- 🚂 **IBM 650** (Frank Hamilton, Ernest Hughes, & James Birkenstock, 1953) — [ibm650](ibm650) / [arch.md](ibm650/arch.md)
- 🏢 **IBM 705** (Werner Buchholz, 1954) — [ibm705](ibm705) / [arch.md](ibm705/arch.md)
- 📇 **IBM 1401** (Chuck Branscomb, 1959) — [ibm1401](ibm1401) / [arch.md](ibm1401/arch.md)
- 🏢 **IBM System/360** (Gene Amdahl, Fred Brooks, & G. A. Blaauw, 1964) — [ibm360](ibm360) / [arch.md](ibm360/arch.md)
- 🔬 **IBM 701** (Nathaniel Rochester, 1952) [replicated from Von Neumann House] — [ibm701](ibm701) / [arch.md](ibm701/arch.md)
- 📈 **IBM 704** (Gene Amdahl, 1954) [replicated from Von Neumann House] — [ibm704](ibm704) / [arch.md](ibm704/arch.md)
- 🚀 **IBM Stretch** (Stephen Dunwell & Werner Buchholz, 1961) [replicated from Explorer House] — [ibmstretch](ibmstretch) / [arch.md](ibmstretch/arch.md)
- 💼 **IBM 6150 ROMP** (John Cocke & IBM Team, 1986) [replicated from RISC House] — [ibm6150](ibm6150) / [arch.md](ibm6150/arch.md)
- 🏢 **IBM System/370 Vector Facility** (IBM, 1985) [replicated from Vector House] — [ibms370vf](ibms370vf) / [arch.md](ibms370vf/arch.md)
- ⚡ **IBM MFAST** (IBM, 1998) [replicated from Array Processor House] — [ibmmfast](ibmmfast) / [arch.md](ibmmfast/arch.md)
- 🌊 **IBM MWave** (IBM, 1992) [replicated from DSP House] — [ibmmwave](ibmmwave) / [arch.md](ibmmwave/arch.md)

### 4. Explorer House
*Highly experimental architectures that introduced radically new paradigms such as micro-programming, multitasking, and indexing/lookahead.*
- 🦓 **STC ZEBRA** (Willem van der Poel, 1958) — [stczebra](stczebra) / [arch.md](stczebra/arch.md)
- 🐂 **Bull Gamma 60** (Machines Bull Team, 1960) — [bullgamma60](bullgamma60) / [arch.md](bullgamma60/arch.md)
- 🚀 **IBM Stretch** (Stephen Dunwell & Werner Buchholz, 1961) — [ibmstretch](ibmstretch) / [arch.md](ibmstretch/arch.md)

### 5. Stack House
*Zero-address architectures utilizing evaluation stacks to minimize instruction size and simplify code generation.*
- 🥞 **Burroughs B5500** (Robert S. Barton, 1964) — [burroughsb5500](burroughsb5500) / [arch.md](burroughsb5500/arch.md)
- 📠 **HP 3000** (HP, 1972) — [hp3000](hp3000) / [arch.md](hp3000/arch.md)
- 📐 **Ethlilith** (Niklaus Wirth, 1980) — [ethlilith](ethlilith) / [arch.md](ethlilith/arch.md)
- ☕ **UCSD Pascal P-Machine** (UCSD, 1978) — [ucsdp](ucsdp) / [arch.md](ucsdp/arch.md)
- ☕ **JVM** (Sun Microsystems, 1995) — [jvm](jvm) / [arch.md](jvm/arch.md)

### 6. Bell House
*Representing the architectural evolution of Digital Equipment Corporation (DEC) systems, led or influenced by C. Gordon Bell.*
- 📉 **DEC PDP-8** (C. Gordon Bell & Edson de Castro, 1965) — [decpdp8](decpdp8) / [arch.md](decpdp8/arch.md)
- 🖥️ **DEC PDP-11** (C. Gordon Bell, 1970) — [decpdp11](decpdp11) / [arch.md](decpdp11/arch.md)
- 💾 **DEC VAX** (C. Gordon Bell & DEC Team, 1977) — [decvax](decvax) / [arch.md](decvax/arch.md)

### 7. General Register House
*Modern paradigms utilizing symmetric register files to decouple calculations from a single accumulator.*
- 🏢 **IBM System/360** (Gene Amdahl, Fred Brooks, & G. A. Blaauw, 1964) — [ibm360](ibm360) / [arch.md](ibm360/arch.md)
- 📟 **Motorola 68000** (Motorola Team, 1979) — [motorola68000](motorola68000) / [arch.md](motorola68000/arch.md)
- 🖥️ **DEC PDP-11** (C. Gordon Bell, 1970) [replicated from Bell House] — [decpdp11](decpdp11) / [arch.md](decpdp11/arch.md)
- 💾 **DEC VAX** (C. Gordon Bell & DEC Team, 1977) [replicated from Bell House] — [decvax](decvax) / [arch.md](decvax/arch.md)
- 💼 **IBM 6150 ROMP** (John Cocke & IBM Team, 1986) [replicated from RISC House] — [ibm6150](ibm6150) / [arch.md](ibm6150/arch.md)
- 🚀 **MIPS I (R2000)** (John Hennessy, 1986) [replicated from RISC House] — [mips1](mips1) / [arch.md](mips1/arch.md)
- 📱 **ARM1** (Sophie Wilson & Steve Furber, 1985) [replicated from RISC House] — [arm1](arm1) / [arch.md](arm1/arch.md)
- 🎓 **Berkeley RISC-I** (David Patterson & Carlo H. Séquin, 1981) [replicated from RISC House] — [berkeleyrisc](berkeleyrisc) / [arch.md](berkeleyrisc/arch.md)
- 🏢 **IBM System/370 Vector Facility** (IBM, 1985) [replicated from Vector House] — [ibms370vf](ibms370vf) / [arch.md](ibms370vf/arch.md)

### 8. Cray House
*Optimized for high-throughput arithmetic, scientific calculations, and pipelining, designed or influenced by Seymour Cray.*
- 📠 **Univac 1103A** (Seymour Cray, 1956) — [univac1103a](univac1103a) / [arch.md](univac1103a/arch.md)
- 🎛️ **CDC 6600 PPU** (Seymour Cray, 1964) — [cdc6600ppu](cdc6600ppu) / [arch.md](cdc6600ppu/arch.md)
- ⚡ **CDC 6600** (Seymour Cray, 1964) — [cdc6600](cdc6600) / [arch.md](cdc6600/arch.md)
- 🌀 **Cray-1** (Seymour Cray, 1976) — [cray1](cray1) / [arch.md](cray1/arch.md)
- 🛰️ **CDC STAR-100** (Control Data Corporation, 1974) [replicated from Vector House] — [cdcstar100](cdcstar100) / [arch.md](cdcstar100/arch.md)

### 9. Microcomputer House
*Constrained accumulator and index-register architectures designed for low-cost, mainstream microcomputing.*
- 🕹️ **MOS 6502** (Chuck Peddle, 1975) — [mos6502](mos6502) / [arch.md](mos6502/arch.md)
- 💻 **Intel 8080A** (Federico Faggin & Masatoshi Shima, 1974) — [intel8080a](intel8080a) / [arch.md](intel8080a/arch.md)
- 🔌 **Motorola 6800** (Tom Bennett, 1974) — [motorola6800](motorola6800) / [arch.md](motorola6800/arch.md)
- 📟 **Motorola 68000** (Motorola Team, 1979) [replicated from General Register House] — [motorola68000](motorola68000) / [arch.md](motorola68000/arch.md)
- 📱 **ARM1** (Sophie Wilson & Steve Furber, 1985) [replicated from RISC House] — [arm1](arm1) / [arch.md](arm1/arch.md)

### 10. RISC House
*Reduced Instruction Set Computer designs prioritizing simplified formats, load-store memory access, and single-cycle executions.*
- 💼 **IBM 801** (John Cocke & IBM Team, 1980) — [ibm801](ibm801) / [arch.md](ibm801/arch.md)
- 💼 **IBM 6150 ROMP** (John Cocke & IBM Team, 1986) — [ibm6150](ibm6150) / [arch.md](ibm6150/arch.md)
- 🚀 **MIPS I (R2000)** (John Hennessy, 1986) — [mips1](mips1) / [arch.md](mips1/arch.md)
- 📱 **ARM1** (Sophie Wilson & Steve Furber, 1985) — [arm1](arm1) / [arch.md](arm1/arch.md)
- 🎓 **Berkeley RISC-I** (David Patterson & Carlo H. Séquin, 1981) — [berkeleyrisc](berkeleyrisc) / [arch.md](berkeleyrisc/arch.md)
- ⚡ **SPARC** (Sun Microsystems, 1987) — [sparc](sparc) / [arch.md](sparc/arch.md)
- 🏹 **PowerPC** (Apple/IBM/Motorola, 1991) — [powerpc](powerpc) / [arch.md](powerpc/arch.md)

### 11. DSP House
*Digital Signal Processors optimized for math, multiply-accumulate operations, and high-performance real-time processing.*
- 🎛️ **NEC µPD7720** (NEC, 1980) — [upd7720](upd7720) / [arch.md](upd7720/arch.md)
- 🧮 **TI TMS32010** (Texas Instruments, 1982) — [tms32010](tms32010) / [arch.md](tms32010/arch.md)
- 🔊 **ADI ADSP-2100** (Analog Devices, 1986) — [adsp2100](adsp2100) / [arch.md](adsp2100/arch.md)
- 🌊 **IBM MWave** (IBM, 1992) — [ibmmwave](ibmmwave) / [arch.md](ibmmwave/arch.md)
- 🧮 **TI TMS320C6000** (Texas Instruments, 1997) [replicated from VLIW House] — [tms320c6k](tms320c6k) / [arch.md](tms320c6k/arch.md)

### 12. GPU House
*Graphics Processing Units and accelerators designed for hardware rasterization, texture mapping, tile-based rendering, and VLIW instruction slots.*
- 🎨 **3dfx Voodoo1** (3dfx Interactive, 1996) — [voodoo1](voodoo1) / [arch.md](voodoo1/arch.md)
- 👁️ **NVIDIA GeForce 256** (NVIDIA, 1999) — [geforce256](geforce256) / [arch.md](geforce256/arch.md)
- 🐉 **ATI Radeon R100** (ATI, 2000) — [radeonr100](radeonr100) / [arch.md](radeonr100/arch.md)
- ⚡ **PowerVR Series 1** (Imagination Technologies, 1996) — [powervr1](powervr1) / [arch.md](powervr1/arch.md)
- 🌀 **ARM Mali-200** (ARM, 2007) — [mali200](mali200) / [arch.md](mali200/arch.md)
- 🔬 **AMD R600** (AMD, 2007) — [amdr600](amdr600) / [arch.md](amdr600/arch.md)

### 13. Bit-Slice House
*Modular processing architectures designed to be cascaded to construct CPUs of custom word lengths.*
- ⚡ **AMD Am2901** (AMD, 1975) — [amd2901](amd2901) / [arch.md](amd2901/arch.md)
- 💾 **Intel 3002** (Intel, 1974) — [intel3002](intel3002) / [arch.md](intel3002/arch.md)
- 🎛️ **NS IMP-16** (National Semiconductor, 1973) — [imp16](imp16) / [arch.md](imp16/arch.md)
- 🔌 **Motorola MC10800** (Motorola, 1976) — [mc10800](mc10800) / [arch.md](mc10800/arch.md)

### 14. Array Processor House
*SIMD, Bit-Serial, and VLIW architectures featuring arrays of parallel processing elements managed by a central control unit.*
- 🖥️ **ILLIAC IV** (UIUC / Burroughs, 1966) — [illiac4](illiac4) / [arch.md](illiac4/arch.md)
- 📡 **ICL DAP** (ICL, 1979) — [icldap](icldap) / [arch.md](icldap/arch.md)
- 🛰️ **Goodyear MPP** (Goodyear / NASA, 1983) — [goodmpp](goodmpp) / [arch.md](goodmpp/arch.md)
- 🕸️ **Thinking Machines CM-1** (Thinking Machines, 1985) — [cm1](cm1) / [arch.md](cm1/arch.md)
- ⚡ **IBM MFAST** (IBM, 1998) — [ibmmfast](ibmmfast) / [arch.md](ibmmfast/arch.md)

### 15. VLIW House
*Architectures utilizing wide instruction words containing multiple parallel operations packed into bundles.*
- ⚡ **Multiflow TRACE** (Josh Fisher, 1987) — [multiflow](multiflow) / [arch.md](multiflow/arch.md)
- 🌀 **Cydrome Cydra 5** (Bob Rau, 1987) — [cydra5](cydra5) / [arch.md](cydra5/arch.md)
- 🧮 **TI TMS320C6000** (Texas Instruments, 1997) — [tms320c6k](tms320c6k) / [arch.md](tms320c6k/arch.md)
- 🔌 **Transmeta Crusoe** (David Ditzel, 2000) — [crusoe](crusoe) / [arch.md](crusoe/arch.md)
- 🏛️ **Intel Itanium** (Intel / HP, 2001) — [itanium](itanium) / [arch.md](itanium/arch.md)

### 16. Vector House
*Designed for high-throughput arithmetic operations on one-dimensional data arrays (vectors), using vector instruction sets and pipelined memory streaming or vector registers.*
- 🌀 **Cray-1** (Seymour Cray, 1976) [replicated from Cray House] — [cray1](cray1) / [arch.md](cray1/arch.md)
- 🛰️ **CDC STAR-100** (Control Data Corporation, 1974) — [cdcstar100](cdcstar100) / [arch.md](cdcstar100/arch.md)
- 📡 **TI ASC** (Texas Instruments, 1972) — [tiasc](tiasc) / [arch.md](tiasc/arch.md)
- 🎛️ **Convex C1** (Convex Computer, 1985) — [convexc1](convexc1) / [arch.md](convexc1/arch.md)
- ⚡ **NEC SX-2** (NEC, 1983) — [necsx2](necsx2) / [arch.md](necsx2/arch.md)
- 🏢 **IBM System/370 Vector Facility** (IBM, 1985) — [ibms370vf](ibms370vf) / [arch.md](ibms370vf/arch.md)

---

## 🛠️ Getting Started

### Prerequisites
Make sure your development container or local path has:
- **Scala** and **sbt**
- **Verilator** and **firtool** (required for Chisel hardware simulation)

### Running All Tests
Execute the entire test suite using sbt:
```bash
sbt test
```

### 🖥️ Writing and Simulating Custom Assembly

BBzoo provides an educational command-line tool to assemble and execute custom programs on the simulated cores.

#### 1. Compile Assembly
Example programs are provided in the [examples](examples) directory. Use the Python assembler to compile assembly files (`.asm`) into hexadecimal files (`.hex`):
```bash
python3 common/sw/zoo_assembler.py -arch <arch_name> -in <source.asm> -out <output.hex>
```

For example, to compile the MIPS Fibonacci program:
```bash
python3 common/sw/zoo_assembler.py -arch mips1 -in examples/mips1_fibonacci.asm -out examples/mips1_fibonacci.hex
```

#### 2. Run the Simulation
Execute the standalone simulator on any core using the compiled hex files. Enable step-by-step tracing with `--trace`:
```bash
sbt "run --arch <arch_name> --hex <path_to_hex_file> [--trace] [--cycles <max_cycles>]"
```

For example, to run the MOS 6502 factorial simulation:
```bash
sbt "run --arch mos --hex examples/mos6502_factorial.hex --trace --cycles 200"
```

The simulator prints the cycle-by-cycle execution trace, register values, and a final performance summary alongside a differential memory dump showing exactly what addresses in memory changed.


### PMU Benchmarks
To compare the execution statistics of the vector addition workload across all 49 architectures, run the comparative profiler test:
```bash
sbt "project root" "testOnly zoo.common.ProfilerSpec"
```
The comparison report will be updated directly at the bottom of `README.md`.

#### Rationale for 4-Element Vector Size
The unified benchmark uses a **4-element vector addition** workload as a universal architectural Rosetta Stone. While a larger vector size (e.g. 64 elements) would better exercise modern vector pipelines (like the Cray-1 or ARM Mali-200), many of the oldest simulated architectures in the zoo (such as the PDP-8, Cambridge EDSAC, and Zuse Z1) operate with extremely tight memory constraints (typically limited to 256 words of addressable memory). 

A 4-element vector size is the optimal design compromise: it is compact enough to fit comfortably within the memory limits of the 1940s-1970s hardware, yet sufficiently expressive to require looping, address calculation, memory reads/writes, and ALU datapath execution, highlighting the direct performance and bandwidth improvements introduced by modern architectural paradigms.

#### Instruction Set Simplification
BBzoo is designed as an educational simulation playground rather than a full-system emulator suite. The simulated hardware cores implement a **minimal, representative subset of instructions** for each historical machine rather than their complete, native instruction sets (omitting complex features like supervisor modes, MMUs, page-fault handling, BCD formats, or hundreds of CISC instruction variants). 

This design choice keeps the RTL implementation of each core clean, highly readable (typically 100–300 lines of Chisel), and focused on the core datapath differences of each architectural paradigm (e.g. Stack, Register, Accumulator) while ensuring sbt and Verilator hardware compilation remains fast and performant.

---

## 📊 Architecture Comparison Report


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
| MOS 6502            | 8                 | 70              | 17                   | 49            | 4             | 4.12 | 72 | 5.7% | 3.12 B/inst | 1.0 regs/inst |
| DEC PDP-8           | 12                | 133              | 33                   | 69            | 20             | 4.03 | 26 | 3.0% | 4.05 B/inst | 1.0 regs/inst |
| DEC PDP-11          | 16                | 81              | 16                   | 31            | 4             | 5.06 | 32 | 4.9% | 4.38 B/inst | 2.5 regs/inst |
| IBM System/360      | 32                | 56              | 12                   | 20            | 4             | 4.67 | 100 | 7.1% | 8.00 B/inst | 1.0 regs/inst |
| Motorola 68000      | 32                | 157              | 28                   | 68            | 8             | 5.61 | 92 | 2.5% | 10.86 B/inst | 2.5 regs/inst |
| Burroughs B5500     | 48                | 46              | 17                   | 25            | 4             | 2.71 | 32 | 13.0% | 10.24 B/inst | 0.5 regs/inst |
| CDC 6600            | 60                | 46              | 17                   | 25            | 4             | 2.71 | 32 | 8.7% | 12.79 B/inst | 1.0 regs/inst |
| Cray-1              | 64 (Vector)       | 40              | 10                   | 18            | 4             | 4.00 | 28 | 10.0% | 17.60 B/inst | 4.5 regs/inst |
| Univac 1103A        | 36                | 38              | 9                   | 21            | 8             | 4.22 | 52 | 10.5% | 14.50 B/inst | 1.0 regs/inst |
| CDC 6600 PPU        | 12                | 38              | 13                   | 21            | 4             | 2.92 | 52 | 10.5% | 2.88 B/inst | 1.0 regs/inst |
| DEC VAX             | 32                | 81              | 16                   | 31            | 4             | 5.06 | 32 | 4.9% | 8.75 B/inst | 2.5 regs/inst |
| Intel 8080A         | 8                 | 78              | 21                   | 53            | 4             | 3.71 | 92 | 5.1% | 2.71 B/inst | 1.0 regs/inst |
| Motorola 6800       | 8                 | 62              | 13                   | 45            | 4             | 4.77 | 92 | 6.5% | 3.77 B/inst | 1.0 regs/inst |
| IBM 6150 ROMP       | 32                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 13.8% | 9.65 B/inst | 2.5 regs/inst |
| MIPS I (R2000)      | 32                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 13.8% | 9.65 B/inst | 2.5 regs/inst |
| ARM1                | 32                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 13.8% | 9.65 B/inst | 2.5 regs/inst |
| Berkeley RISC-I     | 32                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 13.8% | 9.65 B/inst | 2.5 regs/inst |
| HP 3000             | 16                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 13.8% | 4.82 B/inst | 0.5 regs/inst |
| Ethlilith           | 16                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 13.8% | 4.82 B/inst | 0.5 regs/inst |
| UCSD Pascal P-Mach  | 16                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 10.3% | 4.82 B/inst | 0.5 regs/inst |
| NEC uPD7720 DSP     | 16                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 6.9% | 4.82 B/inst | 1.0 regs/inst |
| TI TMS32010 DSP     | 16                | 50              | 13                   | 33            | 4             | 3.85 | 52 | 8.0% | 5.69 B/inst | 1.0 regs/inst |
| ADI ADSP-2100 DSP   | 16                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 6.9% | 4.82 B/inst | 1.0 regs/inst |
| IBM MWave DSP       | 16                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 6.9% | 4.82 B/inst | 1.0 regs/inst |
| 3dfx Voodoo1        | 32                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 6.9% | 9.65 B/inst | 4.5 regs/inst |
| NVIDIA GeForce 256  | 32                | 66              | 19                   | 43            | 4             | 3.47 | 53 | 6.1% | 9.89 B/inst | 4.5 regs/inst |
| ATI Radeon R100     | 32                | 62              | 18                   | 40            | 4             | 3.44 | 53 | 6.5% | 9.78 B/inst | 4.5 regs/inst |
| PowerVR Series 1    | 32                | 62              | 18                   | 40            | 4             | 3.44 | 53 | 6.5% | 9.78 B/inst | 4.5 regs/inst |
| ARM Mali-200 GPU    | 32                | 25              | 5                   | 16            | 4             | 5.00 | 52 | 4.0% | 16.00 B/inst | 4.5 regs/inst |
| AMD R600 GPU        | 32                | 38              | 13                   | 21            | 4             | 2.92 | 52 | 10.5% | 7.69 B/inst | 4.5 regs/inst |
| AMD Am2901          | 16                | 152              | 43                   | 67            | 4             | 3.53 | 41 | 2.6% | 3.30 B/inst | 1.0 regs/inst |
| Intel 3002          | 16                | 152              | 43                   | 67            | 4             | 3.53 | 41 | 2.6% | 3.30 B/inst | 1.0 regs/inst |
| NS IMP-16           | 16                | 478              | 113                   | 253            | 28             | 4.23 | 69 | 0.8% | 4.97 B/inst | 1.0 regs/inst |
| Motorola MC10800    | 16                | 478              | 113                   | 253            | 28             | 4.23 | 69 | 0.8% | 4.97 B/inst | 1.0 regs/inst |
| ILLIAC IV           | 64 (SIMD)         | 32              | 7                   | 8            | 4             | 4.57 | 22 | 25.0% | 13.71 B/inst | 4.5 regs/inst |
| ICL DAP             | 1 (Bit-Serial)    | 548              | 98                   | 128            | 64             | 5.59 | 158 | 0.7% | 3.92 B/inst | 1.0 regs/inst |
| Goodyear MPP        | 1 (Bit-Serial)    | 558              | 100                   | 132            | 64             | 5.58 | 165 | 0.7% | 3.92 B/inst | 1.0 regs/inst |
| Connection Machine  | 1 (Bit-Serial)    | 548              | 98                   | 128            | 64             | 5.59 | 158 | 0.7% | 3.92 B/inst | 1.0 regs/inst |
| IBM MFAST           | 16 (VLIW)         | 39              | 8                   | 8            | 4             | 4.88 | 28 | 20.5% | 3.00 B/inst | 4.5 regs/inst |
| Multiflow TRACE     | 32                | 106              | 31                   | 9            | 4             | 3.42 | 96 | 3.8% | 1.68 B/inst | 3.0 regs/inst |
| Cydrome Cydra 5     | 32                | 94              | 27                   | 9            | 4             | 3.48 | 96 | 4.3% | 1.93 B/inst | 3.0 regs/inst |
| TI TMS320C6000      | 32                | 95              | 31                   | 9            | 4             | 3.06 | 96 | 4.2% | 1.68 B/inst | 3.0 regs/inst |
| Transmeta Crusoe    | 32                | 87              | 31                   | 9            | 4             | 2.81 | 96 | 4.6% | 1.68 B/inst | 3.0 regs/inst |
| Intel Itanium       | 64                | 124              | 31                   | 9            | 4             | 4.00 | 108 | 3.2% | 3.35 B/inst | 3.0 regs/inst |
| CDC STAR-100        | 32                | 24              | 6                   | 8            | 4             | 4.00 | 96 | 16.7% | 8.00 B/inst | 4.0 regs/inst |
| TI ASC              | 32                | 24              | 6                   | 8            | 4             | 4.00 | 96 | 16.7% | 8.00 B/inst | 4.0 regs/inst |
| Convex C1           | 32                | 36              | 10                   | 8            | 4             | 3.60 | 96 | 11.1% | 4.80 B/inst | 4.0 regs/inst |
| NEC SX-2            | 32                | 36              | 10                   | 8            | 4             | 3.60 | 96 | 11.1% | 4.80 B/inst | 4.0 regs/inst |
| IBM S/370 VF        | 32                | 36              | 10                   | 8            | 4             | 3.60 | 96 | 11.1% | 4.80 B/inst | 4.0 regs/inst |
| IBM 801             | 32                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 13.8% | 9.65 B/inst | 2.5 regs/inst |
| SPARC               | 32                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 13.8% | 9.65 B/inst | 2.5 regs/inst |
| PowerPC             | 32                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 13.8% | 9.65 B/inst | 2.5 regs/inst |
| JVM                 | 32                | 58              | 17                   | 37            | 4             | 3.41 | 52 | 13.8% | 9.65 B/inst | 0.5 regs/inst |
