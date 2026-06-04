package zoo.upd7720

import zoo.common.architecture._

/**
 * NEC uPD7720 Architectural Specification.
 * Introduced in 1980.
 */
class Upd7720Architecture extends AccumulatorArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 16
  override val addressWidth: Int = 16
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 8
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = true // Possesses a hardware multiplier
}
