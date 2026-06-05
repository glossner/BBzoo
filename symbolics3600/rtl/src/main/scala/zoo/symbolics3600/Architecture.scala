package zoo.symbolics3600

import zoo.common.architecture._

/**
 * Symbolics 3600 Architectural Specification.
 * A 36-bit tagged architecture Lisp Machine designed by Symbolics in 1983.
 * We model it with a 32-bit word: 4-bit tag + 28-bit payload.
 */
class Symbolics3600Architecture extends StackArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val stackDepth: Int = 16
  override val opCodeWidth: Int = 8
  override val formats = Seq("Stack")
  override val hasMultiplyDivide: Boolean = false
}
