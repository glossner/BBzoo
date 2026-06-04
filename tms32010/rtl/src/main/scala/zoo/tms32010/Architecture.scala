package zoo.tms32010

import zoo.common.architecture._

/**
 * TI TMS32010 Architectural Specification.
 * Introduced in 1982.
 */
class Tms32010Architecture extends AccumulatorArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 16
  override val addressWidth: Int = 16
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 8
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = true // Includes hardware multiplier
}
