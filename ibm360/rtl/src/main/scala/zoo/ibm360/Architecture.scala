package zoo.ibm360

import zoo.common.architecture._

/**
 * IBM System/360 Architectural Specification.
 * Mixes in GeneralRegisterArchitecture, VariableLengthFormat, and FixedPointOperations.
 */
class Ibm360Architecture extends GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 24 // IBM 360 famously uses 24-bit addressing
  override val numGPRs: Int = 16
  override val numFPRs: Int = 4     // 4 double-precision floating-point registers
  override val opCodeWidth: Int = 8
  override val formats: Seq[String] = Seq("RR", "RX", "RS", "SI", "SS")
  override val hasMultiplyDivide: Boolean = true
}
