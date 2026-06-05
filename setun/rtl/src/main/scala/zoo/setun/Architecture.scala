package zoo.setun

import zoo.common.architecture._

/**
 * Setun Architectural Specification.
 * Balanced Ternary logic computer designed at Moscow State University in 1958.
 */
class SetunArchitecture extends StackArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val stackDepth: Int = 16
  override val opCodeWidth: Int = 8
  override val formats = Seq("Stack")
  override val hasMultiplyDivide: Boolean = false
}
