# BBZoo Contributors Guide 🦁

Thank you for your interest in contributing to **BBZoo**! We welcome contributions to expand our zoo of historical computer architectures, improve simulation performance, or enhance our developer toolchain.

To ensure a smooth process for everyone, please follow the guidelines below.

---

## ⚖️ Contributor License Agreement (CLA)

By submitting a pull request or contributing code, documentation, or other assets to this repository, you agree to the following terms:

1. **License Grant**: You hereby grant to the repository owner a royalty-free, perpetual, irrevocable, worldwide, non-exclusive license to use, reproduce, modify, adapt, publish, translate, create derivative works from, distribute, perform, and display your contributions (along with any patent rights necessary to exercise such rights).
2. **License Modification**: You agree that the repository owner reserves the sole right to change, modify, or update the open-source license governing this repository (including all existing and future contributions) at any time. Any new license chosen by the owner will apply retroactively and prospectively to all of your contributions.
3. **Original Work**: You represent that your contributions are your own original creation and that you have the right to grant the licenses described above.

---

## 🌿 Branching Strategy & Workflow

All development and pull requests must target the **`dev`** branch. The `main` branch is reserved for stable releases.

### Contribution Process

1. **Fork the Repository**: Create a personal fork of the repository on GitHub.
2. **Clone the Fork**: Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/BBzoo.git
   ```
3. **Checkout the `dev` Branch**: Make sure you branch off `dev`:
   ```bash
   git checkout dev
   ```
4. **Create a Feature Branch**: Create a descriptive branch name for your work:
   ```bash
   git checkout -b feature/my-amazing-architecture
   ```
5. **Implement and Test**: Add your code, run regression tests, and ensure all checks pass.
6. **Submit a Pull Request**: Push your branch to your fork and submit a PR targeting the **`dev`** branch of the main repository.

---

## 🏛️ Project Directory Structure

BBZoo is structured as an sbt multi-project build. Each architecture is self-contained in its own directory:

- `common/rtl/` — Shared components (e.g., memory interfaces, assemblers, base classes).
- `<architecture_name>/` — Individual processor subprojects (e.g., `ibm360`, `decpdp8`).
  - `rtl/src/main/scala/` — Chisel hardware implementation.
  - `rtl/src/test/scala/` — Simulator test specs.
  - `sw/` — Architecture-specific assembly programs (`.asm`) and compiled hex (`.hex`) files.
- `examples/` — Educational assembly programs and assembler usage guidelines.

---

## 🛠️ Local Setup & Test Verification

### Prerequisites
Make sure your system has the following dependencies installed:
- **Java JDK** (version 17 or higher)
- **sbt** (Scala build tool)
- **Verilator** (required for EphemeralSimulator hardware simulation)
- **Python 3** (required for the assembler scripts)

### Running Tests

Before submitting a Pull Request, run the local regression suite to verify that all hardware cores and assemblers work correctly:

```bash
# Run all core simulation and assembler tests
sbt test

# Run tests only for a specific architecture subproject
sbt "project decpdp8" test

# Run the comparative PMU benchmark report generator
sbt "project root" "testOnly zoo.common.ProfilerSpec"
```

---

## 📐 Hardware Design & Coding Guidelines

To maintain readability and consistency across the 49+ architectures:

1. **Keep it Compact**: Core implementations should be clean, highly readable, and educational (typically 100–300 lines of Chisel). Avoid adding bloated, non-essential system features (like MMUs, supervisor privilege levels, or complex page table walking) unless requested.
2. **Register Naming & Debug Ports**: Provide debug output ports on your top-level `Core` module (e.g., `pc_debug`, `r0_debug`, `hlt`) so the standalone simulator wrapper can log execution traces cycle-by-cycle.
3. **Synthesizable PMUs**: Every new core must contain a Performance Monitoring Unit (PMU) exposed via these I/O pins:
   - `pmu_cycles`: Accumulates elapsed clock cycles.
   - `pmu_insts`: Tracks instruction retirement.
   - `pmu_reads`: Counts memory reads.
   - `pmu_writes`: Counts memory writes.
4. **Assemblers**: Every new architecture must include a corresponding Scala assembler under the `zoo.common.sw` package, and it should be registered in `common/rtl/src/test/scala/zoo/common/AssemblerSpec.scala`.
