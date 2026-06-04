package zoo.zusez1

import zoo.common.architecture._

/**
 * Zuse Z1 Architectural Specification.
 */
class ZuseZ1Architecture extends GeneralRegisterArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 22
  override val addressWidth: Int = 16
  override val numGPRs: Int = 2 // Registers R1 and R2
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 8
  override val instructionWidth: Int = 22
  override val hasMultiplyDivide: Boolean = false
}
