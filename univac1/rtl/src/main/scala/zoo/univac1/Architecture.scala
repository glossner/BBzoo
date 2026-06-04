package zoo.univac1

import zoo.common.architecture._

/**
 * Univac I Architectural Specification.
 */
class Univac1Architecture extends AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 72
  override val addressWidth: Int = 16
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 8
  override val instructionWidth: Int = 72
  override val hasMultiplyDivide: Boolean = false
}
