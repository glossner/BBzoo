package zoo.ibm705

import zoo.common.architecture._

/**
 * IBM 705 Architectural Specification.
 * Variable character machine modeled with 35-bit instructions and 16-bit address paths.
 */
class Ibm705Architecture extends AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 35
  override val addressWidth: Int = 16
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 7
  override val instructionWidth: Int = 35
  override val hasMultiplyDivide: Boolean = false
}
