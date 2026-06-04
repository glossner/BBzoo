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

