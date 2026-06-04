package zoo.ethlilith

import zoo.common.architecture._

/**
 * Ethlilith Architectural Specification.
 * Designed by Niklaus Wirth in 1980.
 */
class EthlilithArchitecture extends StackArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 16
  override val addressWidth: Int = 16
  override val stackDepth: Int = 16
  override val opCodeWidth: Int = 8
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = false
}
