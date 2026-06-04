package zoo.cdc6600

import zoo.common.architecture._

/**
 * CDC 6600 Architectural Specification.
 */
class Cdc6600Architecture extends GeneralRegisterArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 60
  override val addressWidth: Int = 18
  override val numGPRs: Int = 24 // A0-A7 (8), B0-B7 (8), X0-X7 (8)
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 6
  override val instructionWidth: Int = 60 // Instructions are packed or 60-bit word aligned for simplicity in our model
  override val hasMultiplyDivide: Boolean = false
}
