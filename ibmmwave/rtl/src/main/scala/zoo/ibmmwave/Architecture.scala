package zoo.mwave

import zoo.common.architecture._

/**
 * IBM MWave Architectural Specification.
 * Introduced in 1992.
 */
class MwaveArchitecture extends GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 16
  override val addressWidth: Int = 16
  override val numGPRs: Int = 16
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 8
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = true // IBM MWave was a system-level DSP with hardware multiplication.
}
