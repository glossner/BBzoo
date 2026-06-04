package zoo.mos6502

import zoo.common.architecture._

/**
 * MOS 6502 Architectural Specification.
 */
class Mos6502Architecture extends AccumulatorArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 8
  override val addressWidth: Int = 16
  override val hasLinkBit: Boolean = true // Carry flag
  override val opCodeWidth: Int = 8
  override val hasMultiplyDivide: Boolean = false
  override val formats = Seq("Single", "Double", "Triple")
}
