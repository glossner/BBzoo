package zoo.powervr1

import zoo.common.architecture._

/**
 * PowerVR Series 1 Architectural Specification.
 * Introduced in 1996.
 */
class Powervr1Architecture extends GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 16
  override val numGPRs: Int = 4
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 8
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = false
}
