package zoo.decvax

import zoo.common.architecture._

/**
 * DEC VAX Architectural Specification.
 * Word width: 32 bits, Address width: 32 bits.
 * Designed by C. Gordon Bell and the DEC Team in 1977.
 */
class DecvaxArchitecture extends GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 16
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 8
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = false
}
