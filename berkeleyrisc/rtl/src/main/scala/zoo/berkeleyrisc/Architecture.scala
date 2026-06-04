package zoo.berkeleyrisc

import zoo.common.architecture._

/**
 * Berkeley RISC-I Architectural Specification.
 * Designed by David Patterson & Carlo H. Séquin in 1981.
 */
class BerkeleyriscArchitecture extends GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 32
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 8
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = false
}
