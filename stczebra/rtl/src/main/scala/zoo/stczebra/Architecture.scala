package zoo.stczebra

import zoo.common.architecture._

/**
 * STC ZEBRA Architectural Specification.
 * Fixed 33-bit word size, 16-bit address paths.
 * Designed by Willem van der Poel in 1958.
 */
class StczebraArchitecture extends AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 33
  override val addressWidth: Int = 16
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 15 // uses 15 functional bits instead of standard opcode
  override val instructionWidth: Int = 33
  override val hasMultiplyDivide: Boolean = false
}
