package zoo.amdr600

import zoo.common.architecture._

/**
 * AMD R600 Architectural Specification.
 * Introduced in 2007.
 */
class Amdr600Architecture extends GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 16
  override val numGPRs: Int = 4
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 8
  override val formats = Seq("Single")
  override val hasMultiplyDivide: Boolean = false
}
