package zoo.common.architecture

/**
 * Base trait for instruction coding formats.
 */
trait InstructionFormat {
  val opCodeWidth: Int
}

/**
 * Fixed-length instruction format.
 * All instructions are of the same size.
 * Example: DEC PDP-8 (12-bit), Cray-1 (16-bit parcel based).
 */
trait FixedLengthFormat extends InstructionFormat {
  val instructionWidth: Int
}

/**
 * Variable-length instruction format.
 * Instructions can span multiple different lengths depending on the instruction type.
 * Example: IBM System/360 (16-bit, 32-bit, or 48-bit instructions).
 */
trait VariableLengthFormat extends InstructionFormat {
  val formats: Seq[String] // E.g., Seq("RR", "RX", "RS", "SI", "SS") for IBM 360
}
