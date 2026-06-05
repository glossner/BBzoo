package zoo.subleq

import zoo.common.architecture._

/**
 * SUBLEQ Architectural Specification.
 * One-Instruction Set Computer (OISC).
 */
class SubleqArchitecture extends StackArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val stackDepth: Int = 0
  override val opCodeWidth: Int = 0
  override val formats = Seq("Subleq")
  override val hasMultiplyDivide: Boolean = false
}
