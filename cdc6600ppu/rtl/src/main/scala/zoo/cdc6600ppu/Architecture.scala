package zoo.cdc6600ppu

import zoo.common.architecture._

/**
 * CDC 6600 PPU Architectural Specification.
 * Fixed 12-bit word size, 12-bit address paths.
 * Designed by Seymour Cray in 1964.
 */
class Cdc6600ppuArchitecture extends AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 12
  override val addressWidth: Int = 12
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 6
  override val instructionWidth: Int = 12
  override val hasMultiplyDivide: Boolean = false
}
