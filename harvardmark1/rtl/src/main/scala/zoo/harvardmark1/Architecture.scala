package zoo.harvardmark1

import zoo.common.architecture._

/**
 * Harvard Mark I Architectural Specification.
 */
class HarvardMark1Architecture extends GeneralRegisterArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 64
  override val addressWidth: Int = 8
  override val numGPRs: Int = 72
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 8
  override val instructionWidth: Int = 64
  override val hasMultiplyDivide: Boolean = false
}
