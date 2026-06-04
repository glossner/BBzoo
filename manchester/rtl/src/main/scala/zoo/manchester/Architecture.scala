package zoo.manchester

import zoo.common.architecture._

/**
 * Manchester Baby Architectural Specification.
 */
class ManchesterArchitecture extends AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 13
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 3
  override val instructionWidth: Int = 32
  override val hasMultiplyDivide: Boolean = false
}
