package zoo.powerpc

import zoo.common.architecture._

/**
 * PowerPC Architectural Specification.
 * Designed by Apple, IBM, and Motorola (AIM Alliance) in 1991.
 */
class PowerpcArchitecture extends GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 32
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 6
  override val formats = Seq("R", "I")
  override val hasMultiplyDivide: Boolean = false
}
