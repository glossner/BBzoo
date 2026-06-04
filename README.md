# BrooksZoo 🦁

Welcome to **BrooksZoo**, a simulation playground of historical computer architectures implemented in **Chisel / Scala**. The design is inspired by the classic textbook *"Computer Architecture: Concepts and Evolution"* by Gerrit Blaauw and Frederick Brooks.This repository implements **35 unique processor architectures** with fully synthesizable datapaths, synthetic Performance Monitoring Units (PMUs) to track cycles, instruction retirement, memory reads, and memory writes, as well as a unified 4-element Vector Addition benchmark.

---

## 🏛️ Architectural Houses

The architectures in the zoo are organized into ten distinct "houses" based on their core datapath and register organization paradigms:

### 1. Pioneer House
*Characterized by the earliest explorations of stored-program, mechanical, or relay-based calculation.*
- ⚙️ **Babbage Analytical Engine** (Charles Babbage, 1837) — [babbage](file:///home/jglossner/GitRepos/BrooksZoo/babbage) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/babbage/arch.md)
- 🧮 **Harvard Mark I** (Howard Aiken, 1944) — [harvardmark1](file:///home/jglossner/GitRepos/BrooksZoo/harvardmark1) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/harvardmark1/arch.md)
- 💾 **Zuse Z1** (Konrad Zuse, 1938) — [zusez1](file:///home/jglossner/GitRepos/BrooksZoo/zusez1) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/zusez1/arch.md)
- 👶 **Manchester Baby** (Frederic Williams & Tom Kilburn, 1948) — [manchester](file:///home/jglossner/GitRepos/BrooksZoo/manchester) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/manchester/arch.md)
- 📼 **Univac I** (J. Presper Eckert & John Mauchly, 1951) — [univac1](file:///home/jglossner/GitRepos/BrooksZoo/univac1) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/univac1/arch.md)

### 2. Von Neumann House
*Featuring the classic accumulator-based stored-program organization with unified program and data memory.*
- 🏫 **Princeton IAS** (John von Neumann, 1952) — [ias](file:///home/jglossner/GitRepos/BrooksZoo/ias) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/ias/arch.md)
- 📡 **EDSAC** (Maurice Wilkes, 1949) — [edsac](file:///home/jglossner/GitRepos/BrooksZoo/edsac) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/edsac/arch.md)
- 🔬 **IBM 701** (Nathaniel Rochester, 1952) — [ibm701](file:///home/jglossner/GitRepos/BrooksZoo/ibm701) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/ibm701/arch.md)
- 📈 **IBM 704** (Gene Amdahl, 1954) — [ibm704](file:///home/jglossner/GitRepos/BrooksZoo/ibm704) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/ibm704/arch.md)

### 3. IBM House
*Representing the commercial business and variable-word-length scientific computer evolution.*
- 🚂 **IBM 650** (Frank Hamilton, Ernest Hughes, & James Birkenstock, 1953) — [ibm650](file:///home/jglossner/GitRepos/BrooksZoo/ibm650) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/ibm650/arch.md)
- 🏢 **IBM 705** (Werner Buchholz, 1954) — [ibm705](file:///home/jglossner/GitRepos/BrooksZoo/ibm705) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/ibm705/arch.md)
- 📇 **IBM 1401** (Chuck Branscomb, 1959) — [ibm1401](file:///home/jglossner/GitRepos/BrooksZoo/ibm1401) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/ibm1401/arch.md)

### 4. Explorer House
*Highly experimental architectures that introduced radically new paradigms such as micro-programming, multitasking, and indexing/lookahead.*
- 🦓 **STC ZEBRA** (Willem van der Poel, 1958) — [stczebra](file:///home/jglossner/GitRepos/BrooksZoo/stczebra) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/stczebra/arch.md)
- 🐂 **Bull Gamma 60** (Machines Bull Team, 1960) — [bullgamma60](file:///home/jglossner/GitRepos/BrooksZoo/bullgamma60) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/bullgamma60/arch.md)
- 🚀 **IBM Stretch** (Stephen Dunwell, 1961) — [ibmstretch](file:///home/jglossner/GitRepos/BrooksZoo/ibmstretch) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/ibmstretch/arch.md)

### 5. Stack House
*Zero-address architectures utilizing evaluation stacks to minimize instruction size and simplify code generation.*
- 🥞 **Burroughs B5500** (Robert S. Barton, 1964) — [burroughsb5500](file:///home/jglossner/GitRepos/BrooksZoo/burroughsb5500) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/burroughsb5500/arch.md)
- 📠 **HP 3000** (HP, 1972) — [hp3000](file:///home/jglossner/GitRepos/BrooksZoo/hp3000) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/hp3000/arch.md)
- 📐 **Lilith** (Niklaus Wirth, 1980) — [lilith](file:///home/jglossner/GitRepos/BrooksZoo/lilith) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/lilith/arch.md)
- ☕ **UCSD Pascal P-Machine** (UCSD, 1978) — [ucsdp](file:///home/jglossner/GitRepos/BrooksZoo/ucsdp) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/ucsdp/arch.md)

### 6. Bell House
*Representing the architectural evolution of Digital Equipment Corporation (DEC) systems, led or influenced by C. Gordon Bell.*
- 📉 **DEC PDP-8** (C. Gordon Bell & Edson de Castro, 1965) — [decpdp8](file:///home/jglossner/GitRepos/BrooksZoo/decpdp8) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/decpdp8/arch.md)
- 🖥️ **DEC PDP-11** (C. Gordon Bell, 1970) — [decpdp11](file:///home/jglossner/GitRepos/BrooksZoo/decpdp11) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/decpdp11/arch.md)
- 💾 **DEC VAX** (C. Gordon Bell & DEC Team, 1977) — [decvax](file:///home/jglossner/GitRepos/BrooksZoo/decvax) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/decvax/arch.md)

### 7. General Register House
*Modern paradigms utilizing symmetric register files to decouple calculations from a single accumulator.*
- 🏢 **IBM System/360** (Gene Amdahl, Fred Brooks, & G. A. Blaauw, 1964) — [ibm360](file:///home/jglossner/GitRepos/BrooksZoo/ibm360) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/ibm360/arch.md)
- 📟 **Motorola 68000** (Motorola Team, 1979) — [motorola68000](file:///home/jglossner/GitRepos/BrooksZoo/motorola68000) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/motorola68000/arch.md)

### 8. Cray House
*Optimized for high-throughput arithmetic, scientific calculations, and pipelining, designed or influenced by Seymour Cray.*
- 📠 **Univac 1103A** (Seymour Cray, 1956) — [univac1103a](file:///home/jglossner/GitRepos/BrooksZoo/univac1103a) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/univac1103a/arch.md)
- 🎛️ **CDC 6600 PPU** (Seymour Cray, 1964) — [cdc6600ppu](file:///home/jglossner/GitRepos/BrooksZoo/cdc6600ppu) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/cdc6600ppu/arch.md)
- ⚡ **CDC 6600** (Seymour Cray, 1964) — [cdc6600](file:///home/jglossner/GitRepos/BrooksZoo/cdc6600) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/cdc6600/arch.md)
- 🌀 **Cray-1** (Seymour Cray, 1976) — [cray1](file:///home/jglossner/GitRepos/BrooksZoo/cray1) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/cray1/arch.md)

### 9. Microcomputer House
*Constrained accumulator and index-register architectures designed for low-cost, mainstream microcomputing.*
- 🕹️ **MOS 6502** (Chuck Peddle, 1975) — [mos6502](file:///home/jglossner/GitRepos/BrooksZoo/mos6502) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/mos6502/arch.md)
- 💻 **Intel 8080A** (Federico Faggin & Masatoshi Shima, 1974) — [intel8080a](file:///home/jglossner/GitRepos/BrooksZoo/intel8080a) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/intel8080a/arch.md)
- 🔌 **Motorola 6800** (Tom Bennett, 1974) — [motorola6800](file:///home/jglossner/GitRepos/BrooksZoo/motorola6800) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/motorola6800/arch.md)

### 10. RISC House
*Reduced Instruction Set Computer designs prioritizing simplified formats, load-store memory access, and single-cycle executions.*
- 💼 **IBM 6150 ROMP** (John Cocke & IBM Team, 1986) — [ibm6150](file:///home/jglossner/GitRepos/BrooksZoo/ibm6150) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/ibm6150/arch.md)
- 🚀 **MIPS I (R2000)** (John Hennessy, 1986) — [mipsi](file:///home/jglossner/GitRepos/BrooksZoo/mipsi) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/mipsi/arch.md)
- 📱 **ARM1** (Sophie Wilson & Steve Furber, 1985) — [arm1](file:///home/jglossner/GitRepos/BrooksZoo/arm1) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/arm1/arch.md)
- 🎓 **Berkeley RISC-I** (David Patterson & Carlo H. Séquin, 1981) — [berkrisc](file:///home/jglossner/GitRepos/BrooksZoo/berkrisc) / [arch.md](file:///home/jglossner/GitRepos/BrooksZoo/berkrisc/arch.md)

---

## 🛠️ Getting Started

### Prerequisites
Make sure your development container or local path has:
- **Scala** and **sbt**
- **Verilator** and **firtool** (required for Chisel hardware simulation)

Ensure you source the dev container environment script if simulating:
```bash
source /home/jglossner/GitRepos/KryptoNyte/.devcontainer/dev_env.sh
```

### Running All Tests
Execute the entire test suite using sbt:
```bash
sbt test
```

### PMU Benchmarks
To compare the execution statistics of the vector addition workload across all 35 architectures, run the comparative profiler test:
```bash
sbt "project root" "testOnly zoo.common.ProfilerSpec"
```
The comparison report will be written directly to `pmu_report.md`.
