package zoo.decpdp8

import zoo.common.architecture._

/**
 * DEC PDP-8 Architectural Specification.
 * Mixes in AccumulatorArchitecture, FixedLengthFormat, and FixedPointOperations.
 */
class Pdp8Architecture extends AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 12
  override val addressWidth: Int = 12
  override val hasLinkBit: Boolean = true
  override val opCodeWidth: Int = 3
  override val instructionWidth: Int = 12
  override val hasMultiplyDivide: Boolean = false // Standard PDP-8 lacks multiplication/division
}
