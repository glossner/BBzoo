package zoo.babbage

import zoo.common.architecture._

/**
 * Babbage Analytical Engine Architectural Specification.
 */
class BabbageArchitecture extends StackArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 64
  override val addressWidth: Int = 16
  override val stackDepth: Int = 16 // Accumulators / Mill storage depth if needed
  override val opCodeWidth: Int = 8
  override val instructionWidth: Int = 64
  override val hasMultiplyDivide: Boolean = false
}
