package zoo.ibm704

import zoo.common.architecture._

/**
 * IBM 704 Architectural Specification.
 */
class Ibm704Architecture extends AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 36
  override val addressWidth: Int = 15
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 12
  override val instructionWidth: Int = 36
  override val hasMultiplyDivide: Boolean = false
}
