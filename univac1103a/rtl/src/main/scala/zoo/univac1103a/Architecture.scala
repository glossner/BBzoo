package zoo.univac1103a

import zoo.common.architecture._

/**
 * UNIVAC 1103A Architectural Specification.
 * Fixed 36-bit word size, 15-bit address paths.
 * Designed by Seymour Cray in 1956.
 */
class Univac1103aArchitecture extends AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 36
  override val addressWidth: Int = 15
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 6
  override val instructionWidth: Int = 36
  override val hasMultiplyDivide: Boolean = false
}
