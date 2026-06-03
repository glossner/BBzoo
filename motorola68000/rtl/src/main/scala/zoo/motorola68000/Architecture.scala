package zoo.motorola68000

import zoo.common.architecture._

object Motorola68000Architecture extends GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val opCodeWidth: Int = 4
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 16 // D0-D7 (0-7), A0-A7 (8-15)
  override val numFPRs: Int = 0
  override val formats: Seq[String] = Seq("1-Word", "2-Word", "3-Word")
  override val hasMultiplyDivide: Boolean = true
}
