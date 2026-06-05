package zoo.tta

import zoo.common.architecture._

/**
 * TTA (Transport Triggered Architecture) Architectural Specification.
 * Movement-driven execution model.
 */
class TtaArchitecture extends GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 8
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 6
  override val formats = Seq("Move")
  override val hasMultiplyDivide: Boolean = false
}
