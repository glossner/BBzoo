package zoo.bullgamma60

import zoo.common.architecture._

/**
 * Bull Gamma 60 Architectural Specification.
 * Fixed 24-bit instruction word, 16-bit address paths.
 * Designed by Compagnie des Machines Bull in 1960.
 */
class Bullgamma60Architecture extends AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 24
  override val addressWidth: Int = 16
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 6
  override val instructionWidth: Int = 24
  override val hasMultiplyDivide: Boolean = false
}
