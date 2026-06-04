package zoo.arm1

import zoo.common.architecture._

/**
 * ARM1 Architectural Specification.
 * Designed by Sophie Wilson & Steve Furber in 1985.
 */
class Arm1Architecture extends GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 16
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 8
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = false
}
