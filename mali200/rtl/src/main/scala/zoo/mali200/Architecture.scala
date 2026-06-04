package zoo.mali200

import zoo.common.architecture._

/**
 * ARM Mali-200 Architectural Specification.
 * Introduced in 2007.
 */
class Mali200Architecture extends VectorArchitecture {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 16
  override val numGPRs: Int = 4
  override val numFPRs: Int = 0
  override val vectorLength: Int = 4
  override val numVecRegs: Int = 4
}
