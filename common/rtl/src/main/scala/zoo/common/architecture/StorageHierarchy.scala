package zoo.common.architecture

import chisel3._

/**
 * Base trait for all computer architectures in the zoo.
 * Defines the fundamental parameters of the design space.
 */
trait StorageArchitecture {
  val wordWidth: Int
  val addressWidth: Int
}

/**
 * Stack Architecture (zero-address storage hierarchy).
 * Operands are implicitly on top of a stack.
 * Example: Burroughs B5500.
 */
trait StackArchitecture extends StorageArchitecture {
  val stackDepth: Int
}

/**
 * Accumulator Architecture (single-address storage hierarchy).
 * Operands are implicitly in a single special register (Accumulator).
 * Example: DEC PDP-8.
 */
trait AccumulatorArchitecture extends StorageArchitecture {
  val hasLinkBit: Boolean // E.g., PDP-8 has a 1-bit Link register for carry
}

/**
 * General-Purpose Register (GPR) Architecture (double/multi-address storage).
 * Operands are stored in a file of addressable registers.
 * Example: IBM System/360, Cray-1.
 */
trait GeneralRegisterArchitecture extends StorageArchitecture {
  val numGPRs: Int
  val numFPRs: Int // Floating point registers
}

/**
 * Vector Register Architecture.
 * Support for vector execution on arrays of data.
 * Example: Cray-1.
 */
trait VectorArchitecture extends GeneralRegisterArchitecture {
  val vectorLength: Int // Number of elements per vector register
  val numVecRegs: Int   // Number of vector registers
}
