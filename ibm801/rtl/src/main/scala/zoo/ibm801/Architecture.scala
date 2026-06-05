package zoo.ibm801

import zoo.common.architecture._

/**
 * IBM 801 Architectural Specification.
 * Designed by John Cocke & IBM Team in 1980.
 */
class Ibm801Architecture extends GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 32
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 6
  override val formats = Seq("R", "I")
  override val hasMultiplyDivide: Boolean = false
}
