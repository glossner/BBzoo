package zoo.ibmstretch

import zoo.common.architecture._

/**
 * IBM Stretch (IBM 7030) Architectural Specification.
 * Fixed 64-bit word size, 20-bit address paths.
 * Designed by Stephen Dunwell in 1961.
 */
class IbmstretchArchitecture extends AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 64
  override val addressWidth: Int = 20
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 8
  override val instructionWidth: Int = 64
  override val hasMultiplyDivide: Boolean = false
}
