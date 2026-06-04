package zoo.burroughsb5500

import zoo.common.architecture._

/**
 * Burroughs B5500 Architectural Specification.
 */
class B5500Architecture extends StackArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 48
  override val addressWidth: Int = 15
  override val stackDepth: Int = 16
  override val opCodeWidth: Int = 8
  override val instructionWidth: Int = 48 // Use 48-bit instruction size
  override val hasMultiplyDivide: Boolean = true
}
