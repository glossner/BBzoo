package zoo.intel8080a

import zoo.common.architecture._

/**
 * Intel 8080A Architectural Specification.
 * Designed by Federico Faggin & Masatoshi Shima in 1974.
 */
class Intel8080aArchitecture extends AccumulatorArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 8
  override val addressWidth: Int = 16
  override val hasLinkBit: Boolean = true // Carry flag
  override val opCodeWidth: Int = 8
  override val hasMultiplyDivide: Boolean = false
  override val formats = Seq("Single", "Double", "Triple")
}
