package zoo.cambridgeedsac

import zoo.common.architecture._

/**
 * EDSAC Architectural Specification.
 */
class CambridgeedsacArchitecture extends AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 17
  override val addressWidth: Int = 10
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 5
  override val instructionWidth: Int = 17
  override val hasMultiplyDivide: Boolean = false
}
