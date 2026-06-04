package zoo.mipsi

import zoo.common.architecture._

/**
 * MIPS I (R2000) Architectural Specification.
 * Designed by John L. Hennessy & MIPS team in 1986.
 */
class MipsiArchitecture extends GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 32
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 6
  override val formats = Seq("R", "I", "J")
  override val hasMultiplyDivide: Boolean = false
}
