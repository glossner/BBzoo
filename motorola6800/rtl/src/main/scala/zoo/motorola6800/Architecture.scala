package zoo.motorola6800

import zoo.common.architecture._

/**
 * Motorola 6800 Architectural Specification.
 * Designed by Tom Bennett in 1974.
 */
class Motorola6800Architecture extends AccumulatorArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 8
  override val addressWidth: Int = 16
  override val hasLinkBit: Boolean = true // Carry flag
  override val opCodeWidth: Int = 8
  override val hasMultiplyDivide: Boolean = false
  override val formats = Seq("Single", "Double", "Triple")
}
