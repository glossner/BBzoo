package zoo.mitdataflow

import zoo.common.architecture._

/**
 * MIT Tagged-Token Dataflow Architectural Specification.
 * Data-driven architecture developed by Arvind at MIT in the late 1970s / early 1980s.
 */
class MitDataflowArchitecture extends StackArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val stackDepth: Int = 0
  override val opCodeWidth: Int = 8
  override val formats = Seq("Dataflow")
  override val hasMultiplyDivide: Boolean = false
}
