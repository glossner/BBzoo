# 06/04/2026 11:15 Instruction Simplification Rationale
- Documented the design rationale for implementing minimal synthesizable instruction subsets in README.md, emphasizing educational readability, build performance, and baseline architectural paradigms.

# 06/04/2026 11:12 Purpose-Conforming Assembly File Generation
- Generated a dedicated assembly file (`sw/purpose_stress.asm`) and compiled hex file (`sw/purpose_stress.hex`) for all 45 simulated architectures inside their respective subdirectories.
- Verified successful assembly of all 45 stress programs.

# 06/04/2026 11:00 Documented Design Purposes & Assembly Stress Programs
- Documented the primary historical design purpose of all 45 simulated architectures directly in their corresponding `arch.md` files.
- Provided a purpose-conforming assembly language program inside each machine's `arch.md` file that stresses its simulated instruction set (sharing program layouts across same-purpose machines to the extent possible).
- Verified assembly and execution of the new purpose-conforming stress programs on the simulated cores using the Python assembler and standalone Scala CLI simulator runner.

# 06/04/2026 10:45 Educational CLI Simulator & Target Assembler Examples
- Implemented a standalone command-line simulator runner (`SimulatorApp.scala`) to execute user assembly hex files with cycle-by-cycle register and memory tracing.
- Created `/examples` directory with sample assembly programs (`mips1_fibonacci.asm`, `mos6502_factorial.asm`, and `intel8080a_loop.asm`) representing MIPS RISC, MOS Accumulator, and Intel Accumulator loop patterns.
- Verified execution correctness of all examples via the `SimulatorApp` runner.
- Documented custom assembly compilation and simulation procedures in the root `README.md` and `examples/README.md`.

# 06/04/2026 10:34 PMU Metrics Enhancement & Benchmark Documentation
- Enhanced the comparative architecture report (`pmu_report.md`) with four advanced hardware execution metrics: Code Footprint (words), ALU Duty Cycle, Memory Bandwidth Efficiency (Bytes/Instruction), and Register Port Stress.
- Updated `ProfilerSpec.scala` to analytically compute and output the new metrics based on code size, word width, register footprints, and memory transactions.
- Documented the design rationale for the 4-element vector addition benchmark in `README.md`, explaining the memory constraints of early historical computer architectures and how it balances compatibility with representative execution profiles.

# 06/04/2026 09:54 GPU House Expansion & Architecture Renaming: 3dfx Voodoo1, NVIDIA GeForce 256, ATI Radeon R100, PowerVR Series 1, ARM Mali-200, AMD R600
- Implemented six iconic early graphics architectures under the new **GPU House**: 3dfx Voodoo1, NVIDIA GeForce 256, ATI Radeon R100, PowerVR Series 1, ARM Mali-200, and AMD R600.
- Built synthesizable multi-cycle datapath cores, instruction decoders, and sbt test suites for all 6 GPU architectures.
- Added synthesizable Performance Monitoring Units (PMUs) to all 6 new cores to track cycles, instruction retirement, and memory read/write traffic.
- Developed Python and Scala assemblers for all 6 new GPU architectures and integrated them into the global assembler registries.
- Created architectural documentation (`arch.md`) and assembly vector addition test codes for all 6 architectures.
- Integrated the new GPU cores into the unified 4-element Vector Addition benchmark (`ProfilerSpec.scala`), compiling them, verifying correct computation, and generating comparison PMU metrics.
- Reorganized `README.md` to catalog all 45 architectures, establishing the new **GPU House** category.
- Renamed and refactored seven existing architectures for consistency and naming alignment:
  - `mwave` -> `ibmmwave`
  - `ias` -> `princetonias`
  - `manchester` -> `manchestermu1`
  - `berkrisc` -> `berkeleyrisc`
  - `edsac` -> `cambridgeedsac`
  - `mipsi` -> `mips1`
  - `lilith` -> `ethlilith`

# 06/04/2026 08:35 DSP House Expansion: NEC uPD7720, TI TMS32010, ADI ADSP-2100, and IBM MWave
- Implemented four classic digital signal processors under the new **DSP House**: NEC µPD7720, TI TMS32010, ADI ADSP-2100, and IBM MWave.
- Built synthesizable multi-cycle datapath cores, instruction decoders, and sbt test suites for all 4 DSP architectures.
- Added synthesizable Performance Monitoring Units (PMUs) to all 4 new cores to track cycles, instruction retirement, and memory read/write traffic.
- Developed Python and Scala assemblers for all 4 new DSP architectures and integrated them into the global assembler registries.
- Created architectural documentation (`arch.md`) and assembly vector addition test codes for all 4 architectures.
- Integrated the new DSP cores into the unified 4-element Vector Addition benchmark (`ProfilerSpec.scala`), compiling them, verifying correct computation, and generating comparison PMU metrics.
- Reorganized `README.md` to catalog all 39 architectures, establishing the new **DSP House** category.

# 06/04/2026 08:30 RISC House and Stack House Expansion: MIPS I, ARM1, Berkeley RISC-I, HP 3000, Lilith, and UCSD Pascal P-Machine
- Implemented six new classic architectures to complete the RISC House and Stack House expansions: MIPS I (R2000), ARM1, Berkeley RISC-I, HP 3000, Lilith, and UCSD Pascal P-Machine.
- Added synthesizable Performance Monitoring Units (PMUs) to all 6 new cores to track cycles, instruction retirement, and memory read/write traffic.
- Built Scala and Python assemblers for all 6 new architectures and registered them globally.
- Created architectural documentation (`arch.md`) for all 6 new machines detailing introduction year, designers, unique features, and instruction formats.
- Expanded the unified 4-element Vector Addition benchmark (`ProfilerSpec.scala`) to include the 6 new cores, verifying correct computation and generating comparative PMU execution metrics.
- Reorganized `README.md` to catalog all 35 machines, creating the **Stack House** (grouping Burroughs B5500, HP 3000, Lilith, UCSD Pascal P-Machine) and the **RISC House** (grouping IBM 6150 ROMP, MIPS I R2000, ARM1, Berkeley RISC-I).

# 06/04/2026 08:05 Microcomputer House Expansion: Intel 8080A, Motorola 6800, and IBM 6150
- Implemented three new Microcomputer House architectures (Intel 8080A, Motorola 6800, and IBM 6150 ROMP) with fully functional multi-cycle datapath cores, instruction decoders, and test suites.
- Added synthesizable Performance Monitoring Units (PMUs) to all 3 new cores to track cycles, instruction retirement, and memory read/write traffic.
- Designed unique custom execution paradigms for each machine: 8-bit accumulator and little-endian address decoding for Intel 8080A, 8-bit accumulator and big-endian address decoding for Motorola 6800, and 32-bit load-store RISC register-to-register logic for IBM 6150 ROMP.
- Built Scala and Python assemblers for all 3 new architectures and integrated them into the global pluggable assembler registry.
- Created architectural documentation (`arch.md`) for all 3 new machines detailing introduction year, designers (Federico Faggin & Masatoshi Shima, Tom Bennett, John Cocke & IBM Team), unique architectural features, instruction formats, and implemented/unimplemented instructions.
- Expanded the unified 4-element Vector Addition benchmark (`ProfilerSpec.scala`) to include the 3 new cores, verifying correct computation and generating comparative PMU execution metrics.
- Updated `README.md` to catalog all 29 machines and group MOS 6502, Intel 8080A, Motorola 6800, and IBM 6150 ROMP under the **Microcomputer House**.

# 06/04/2026 07:50 Bell House Completion: DEC VAX
- Implemented the classic DEC VAX CISC architecture with a fully functional multi-cycle datapath core, instruction decoder, and test suite.
- Added synthesizable Performance Monitoring Unit (PMU) to the VAX core to track cycles, instruction retirement, and memory read/write traffic.
- Designed orthogonal operand specifier decoding supporting register, register deferred, autoincrement, and PC-relative immediate addressing modes packed into a word-aligned 32-bit instruction layout.
- Built Scala and Python assemblers for VAX supporting MOVL, ADDL2, SUBL2, and HALT instructions, and integrated them into the global assembler registry.
- Created architectural documentation (`decvax/arch.md`) detailing designer (C. Gordon Bell & DEC Team), introduction year (1977), unique CISC features, instruction formatting, and implemented/unimplemented instructions.
- Expanded the unified 4-element Vector Addition benchmark (`ProfilerSpec.scala`) to include DEC VAX, verifying correct computation and generating comparative PMU execution metrics.
- Updated `README.md` to catalog all 26 machines and group DEC PDP-8, DEC PDP-11, and DEC VAX under the newly defined **Bell House**.

# 06/04/2026 07:45 Cray House Expansion: Univac 1103A and CDC 6600 PPU
- Implemented two new Cray House architectures (Univac 1103A and CDC 6600 PPU) with fully functional multi-cycle datapath cores, instruction decoders, and test suites.
- Added synthesizable Performance Monitoring Units (PMUs) to both new cores to track cycles, instruction retirement, and memory read/write traffic.
- Designed unique custom execution paradigms for each machine: two-address memory-to-memory datapath for Univac 1103A, and 12-bit accumulator logic with 6-bit direct addressing for CDC 6600 PPU.
- Built Scala and Python assemblers for both new architectures and integrated them into the global pluggable assembler registry.
- Created architectural documentation (`arch.md`) for both new machines detailing introduction year, designer (Seymour Cray), unique architectural features, instruction formats, and implemented/unimplemented instructions.
- Expanded the unified 4-element Vector Addition benchmark (`ProfilerSpec.scala`) to include the two new cores, verifying correct computation and generating comparative PMU execution metrics.
- Updated `README.md` to catalog all 25 machines, organizing them by their architectural families/houses (specifically creating the new Cray House grouping containing Univac 1103A, CDC 6600 PPU, CDC 6600, and Cray-1) while preserving the flat `companyMachine` top-level directory structure.

# 06/04/2026 06:45 Explorer House Expansion: STC ZEBRA, Bull Gamma 60, and IBM Stretch
- Implemented three new Explorer House CPU architectures (STC ZEBRA, Bull Gamma 60, and IBM Stretch) with fully functional multi-cycle datapath cores, instruction decoders, and test suites.
- Added synthesizable Performance Monitoring Units (PMUs) to all 3 new cores to track cycles, instruction retirement, and memory read/write traffic.
- Designed unique custom execution paradigms for each machine: functional-bits microprogramming control layout for STC ZEBRA, multithreaded time-multiplexing concurrent thread coordination (FORK/JOIN) for Bull Gamma 60, and 16 index register address calculations for IBM Stretch.
- Built Scala and Python assemblers for all 3 new architectures and integrated them into the global pluggable assembler registry.
- Created architectural documentation (`arch.md`) for all 3 new machines detailing introduction year, designers (Willem van der Poel, Compagnie des Machines Bull, Stephen Dunwell), unique architectural features, instruction formats, and implemented/unimplemented instructions.
- Expanded the unified 4-element Vector Addition benchmark (`ProfilerSpec.scala`) to include the 3 new cores, verifying correct computation and generating comparative PMU execution metrics.
- Updated `README.md` to catalog all 23 machines, organizing them by their architectural families/houses (specifically creating the new Explorer House grouping containing STC ZEBRA, Bull Gamma 60, IBM Stretch, and Burroughs B5500) while preserving the flat `companyMachine` top-level directory structure.

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

# 06/03/2026 21:08 Expansion of BBZoo with Burroughs B5500, DEC PDP-11, CDC 6600, and MOS 6502
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

