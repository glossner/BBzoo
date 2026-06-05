package zoo.ibm1620

import zoo.common.architecture._

/**
 * IBM 1620 Architectural Specification.
 * Variable word length, no-adder table-lookup computer designed by IBM in 1959.
 */
class Ibm1620Architecture extends GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 32
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 6
  override val formats = Seq("R", "I")
  override val hasMultiplyDivide: Boolean = false
}
