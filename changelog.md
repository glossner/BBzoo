# 06/04/2026 06:30 IBM House Expansion: IBM 650, IBM 705, and IBM 1401
- Implemented three classic IBM House CPU architectures (IBM 650, IBM 705, and IBM 1401) with fully functional multi-cycle datapath cores, instruction decoders, and test suites.
- Added synthesizable Performance Monitoring Units (PMUs) to all 3 new cores to track cycles, instruction retirement, and memory read/write traffic.
- Designed unique custom execution paradigms for each machine: next-instruction-address drum chaining for IBM 650, character-oriented 35-bit word logic for IBM 705, and memory-to-memory evaluation with no accumulator register for IBM 1401.
- Built Scala and Python assemblers for all 3 new architectures and integrated them into the pluggable assembler framework.
- Created architectural documentation (`arch.md`) for all 3 new machines detailing introduction year, designers (Frank Hamilton, Ernest Hughes, James Birkenstock, Werner Buchholz, Chuck Branscomb), unique architectural features, instruction formats, and implemented/unimplemented instructions.
- Expanded the unified 4-element Vector Addition benchmark (`ProfilerSpec.scala`) to include the 3 new cores, verifying correct computation and generating comparative PMU execution metrics.
- Updated `README.md` to catalog all 20 machines, organizing them by their architectural families/houses while preserving the flat `companyMachine` top-level directory structure.

# 06/04/2026 05:55 Von Neumann House Expansion: Princeton IAS, EDSAC, IBM 701, and IBM 704
- Implemented four classic Von Neumann House CPU architectures (Princeton IAS, EDSAC, IBM 701, and IBM 704) with fully functional multi-cycle datapath cores, instruction decoders, and test suites.
- Added synthesizable Performance Monitoring Units (PMUs) to all 4 new cores to track cycles, instruction retirement, and memory read/write traffic.
- Built Scala and Python assemblers for all 4 new architectures and integrated them into the pluggable assembler toolchain.
- Resolved Scala array indexing syntax in `Ibm704Assembler.scala` by replacing bracket access with parentheses.
- Created architectural documentation (`arch.md`) for all 4 new machines detailing introduction year, designers (John von Neumann, Maurice Wilkes, Nathaniel Rochester, Gene Amdahl), unique architectural features, instruction formats, and implemented/unimplemented instructions.
- Expanded the unified 4-element Vector Addition benchmark (`ProfilerSpec.scala`) to include the 4 new cores, verifying correct computation and generating comparative PMU execution metrics.
- Updated `README.md` to catalog all 17 machines, organizing them by their architectural families/houses while preserving the flat `companyMachine` top-level directory structure.

# 06/04/2026 05:40 Pioneer House Expansion: Babbage, Harvard Mark I, Zuse Z1, Manchester Baby, and Univac I
- Implemented five pioneer-era CPU architectures (Babbage Analytical Engine, Harvard Mark I, Zuse Z1, Manchester Baby, and Univac I) complete with instruction decoders, cores with synthesizable PMUs, and test suites.
- Added Scala and Python assemblers for all 5 architectures and integrated them into the pluggable assembler framework.
- Optimized 72-bit word assembly inside the Univac I Scala assembler using `BigInt` to prevent 64-bit integer overflow.
- Resolved sign-extension/unsigned lit boundary verification inside the Manchester Baby simulation test suite.
- Integrated all 5 new machines into the unified 4-element Vector Addition benchmark (`ProfilerSpec.scala`) and generated a consolidated 13-core performance report.

# 06/03/2026 21:08 Expansion of BrooksZoo with Burroughs B5500, DEC PDP-11, CDC 6600, and MOS 6502
- Implemented Burroughs B5500 (Stack), DEC PDP-11 (Orthogonal CISC), CDC 6600 (implicit Load/Store via Address Registers), and MOS 6502 (Accumulator-Index) architectures.
- Added instruction decoders, cores with synthesizable performance monitoring units (PMUs), and test suites for all 4 new projects.
- Developed Scala and Python assemblers for all 4 new architectures.
- Unified the 4-element Vector Addition benchmark across all 8 processors and updated `ProfilerSpec.scala` to run them and output a consolidated PMU comparison report.

# 06/03/2026 19:05 Unified Vector Addition Benchmark and IBM 360 Core Bug Fix
- Designed and implemented a unified 4-element Vector Addition benchmark program across DEC PDP-8, IBM System/360, Cray-1, and Motorola 68000.
- Fixed a hardware logic bug in `Ibm360Core` where RX arithmetic instructions incorrectly bypassed the ALU during writeback (MemToReg routing).
- Added label symbol resolution inside `DATA` directives for both Python and Scala assemblers.
- Added `A` (Add) and `S` (Subtract) RX instruction opcodes to both IBM System/360 assemblers.
- Updated all unit tests and the architecture profiler spec to verify benchmark correctness and output the performance comparison matrix.

# 06/03/2026 18:50 Synthesizable PMU Hardware and Unified Architecture Profiler
- Added synthesizable Performance Monitoring Units (PMUs) to `Pdp8Core`, `Ibm360Core`, `Cray1Core`, and `M68kCore` to track cycle counts, retired instructions, and memory reads/writes.
- Created `ProfilerSpec.scala` in the root project to execute all four cores, query their hardware PMU registers, and output a consolidated comparative markdown report comparing CPI and memory traffic.
- Updated `build.sbt` to allow the root project to depend on all zoo subprojects for unified test-bench access.

# 06/03/2026 16:10 Custom Python AST Compiler for Exotic Zoo Architectures
- Implemented `zoo_compiler.py` parsing Python source files via the native `ast` parser and targeting DEC PDP-8 and Cray-1 assembly.
- Added `Pdp8Codegen` subclass supporting accumulator-based scalar assignments, binary addition/AND expressions, variable allocation, and literal constants.
- Added `Cray1Codegen` subclass supporting vector register instructions, loads/stores, vector addition, and vector length control.
- Developed `verify_compiler.py` to automate compilation, assembly, and test verification against reference core hex files.

# 06/03/2026 16:04 ELF Loading Toolchain and Cross-Compilation Integration
- Integrated `pyelftools` dependency to enable parsing ELF object and linked binary files.
- Implemented `elf_loader.py` utility to extract section data from relocatable `.o` files and convert them to word-size formatted simulation hex files.
- Implemented `verify_elf.py` validating a complete Motorola 68000 cross-compilation pipeline using `clang -target m68k-elf -c` and the Python ELF loader.

# 06/03/2026 15:54 Python-based Architecture Taxonomy and uv Dependency Management
- Implemented Python-based architecture taxonomy class traits (`StorageArchitecture`, `InstructionFormat`, `Operations`) under `common/sw/architecture/` mirroring Scala OOP traits.
- Refactored the four Python-based assemblers (`pdp8`, `ibm360`, `cray1`, `m68k`) to inherit from the appropriate taxonomy traits and declare properties.
- Configured project dependencies and metadata using `pyproject.toml` and locked dependencies using `uv`.
- Verified that both the Python and Scala assemblers and simulation tests pass successfully.

# 06/03/2026 15:32 Scala-Based Pluggable Assembler Framework
- Implemented Scala-based pluggable assembler framework (`BaseAssembler.scala`, `Assembler.scala`) and machine-specific sub-assemblers (`Pdp8Assembler`, `Ibm360Assembler`, `Cray1Assembler`, `M68kAssembler`) directly mixing in Scala hardware taxonomy traits.
- Added `AssemblerSpec.scala` test suite to automatically verify assembled assembler outputs against reference hex files.

# 06/03/2026 15:19 Motorola 68000 Core Implementation, Integration of Existing Architectures, and Bug Fixes
- Implemented Motorola 68000 architecture, instruction decoder, multi-cycle datapath core, and simulation tests.
- Integrated and verified existing Cray-1, DEC PDP-8, and IBM System/360 cores with Chisel 7.12.0.
- Resolved simulation test failures for all architectures by implementing explicit simulation reset cycles.
- Fixed destination register and immediate value mapping for Load Immediate instructions in the Cray-1 core.

