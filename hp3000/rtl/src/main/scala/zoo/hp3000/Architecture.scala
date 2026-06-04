package zoo.hp3000

import zoo.common.architecture._

/**
 * HP 3000 Architectural Specification.
 * Introduced in 1972.
 */
class Hp3000Architecture extends StackArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 16
  override val addressWidth: Int = 16
  override val stackDepth: Int = 16
  override val opCodeWidth: Int = 8
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = false
}
