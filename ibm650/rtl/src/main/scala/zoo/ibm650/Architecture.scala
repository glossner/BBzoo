package zoo.ibm650

import zoo.common.architecture._

/**
 * IBM 650 Architectural Specification.
 * Fixed 40-bit word (10 decimal digits), 16-bit address paths.
 */
class Ibm650Architecture extends AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 40
  override val addressWidth: Int = 16
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 8
  override val instructionWidth: Int = 40
  override val hasMultiplyDivide: Boolean = false
}
