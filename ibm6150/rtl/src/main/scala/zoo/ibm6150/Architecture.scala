package zoo.ibm6150

import zoo.common.architecture._

/**
 * IBM 6150 (ROMP) Architectural Specification.
 * Designed by John Cocke & IBM Team in 1986.
 */
class Ibm6150Architecture extends GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 16
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 8
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = false
}
