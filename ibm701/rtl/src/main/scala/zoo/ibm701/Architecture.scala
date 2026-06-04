package zoo.ibm701

import zoo.common.architecture._

/**
 * IBM 701 Architectural Specification.
 */
class Ibm701Architecture extends AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 36
  override val addressWidth: Int = 12
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 5
  override val instructionWidth: Int = 36
  override val hasMultiplyDivide: Boolean = false
}
