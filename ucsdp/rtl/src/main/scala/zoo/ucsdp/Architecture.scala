package zoo.ucsdp

import zoo.common.architecture._

/**
 * UCSD Pascal P-Machine Architectural Specification.
 * Designed by UCSD in 1978.
 */
class UcsdpArchitecture extends StackArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 16
  override val addressWidth: Int = 16
  override val stackDepth: Int = 16
  override val opCodeWidth: Int = 8
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = false
}
