package zoo.jvm

import zoo.common.architecture._

/**
 * JVM Architectural Specification.
 * Designed by Sun Microsystems in 1995.
 */
class JvmArchitecture extends StackArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val stackDepth: Int = 16
  override val opCodeWidth: Int = 8
  override val formats = Seq("Stack")
  override val hasMultiplyDivide: Boolean = false
}
