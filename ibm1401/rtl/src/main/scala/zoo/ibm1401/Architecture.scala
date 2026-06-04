package zoo.ibm1401

import zoo.common.architecture._

/**
 * IBM 1401 Architectural Specification.
 * Memory-to-Memory decimal machine modeled with 36-bit word size and 12-bit address paths.
 */
class Ibm1401Architecture extends AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 36
  override val addressWidth: Int = 12
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 8
  override val instructionWidth: Int = 36
  override val hasMultiplyDivide: Boolean = false
}
