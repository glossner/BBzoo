package zoo.princetonias

import zoo.common.architecture._

/**
 * Princeton IAS Architectural Specification.
 */
class PrincetoniasArchitecture extends AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 40
  override val addressWidth: Int = 12
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 8
  override val instructionWidth: Int = 40
  override val hasMultiplyDivide: Boolean = false
}
