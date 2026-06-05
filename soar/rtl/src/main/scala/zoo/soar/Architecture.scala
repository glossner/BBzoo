package zoo.soar

import zoo.common.architecture._

/**
 * SOAR (Smalltalk On A RISC) Architectural Specification.
 * Designed at UC Berkeley in 1984.
 */
class SoarArchitecture extends GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 32
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 6
  override val formats = Seq("R", "I")
  override val hasMultiplyDivide: Boolean = false
}
