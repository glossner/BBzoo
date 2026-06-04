package zoo.adsp2100

import zoo.common.architecture._

/**
 * ADI ADSP-2100 Architectural Specification.
 * Introduced in 1986.
 */
class Adsp2100Architecture extends GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 16
  override val addressWidth: Int = 16
  override val numGPRs: Int = 16
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 8
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = true // Contains hardware MAC multiplier
}
