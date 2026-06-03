package zoo.cray1

import zoo.common.architecture._

/**
 * Cray-1 Architectural Specification.
 * Mixes in VectorArchitecture, FixedLengthFormat, and VectorOperations.
 */
class Cray1Architecture extends VectorArchitecture with FixedLengthFormat with VectorOperations with FixedPointOperations with FloatingPointOperations {
  override val wordWidth: Int = 64
  override val addressWidth: Int = 24
  override val numGPRs: Int = 8 // Cray-1 has 8 A registers (24-bit) and 8 S registers (64-bit)
  override val numFPRs: Int = 0 // Floating point is integrated into S/V registers
  override val opCodeWidth: Int = 7 // g (4-bit) and h (3-bit) opcode fields
  override val instructionWidth: Int = 16 // 1 parcel (16-bit) or 2 parcel (32-bit)
  override val vectorLength: Int = 64 // 64 elements per vector register
  override val numVecRegs: Int = 8 // 8 vector registers (V0-V7)
  override val hasMultiplyDivide: Boolean = true
  override val fpuPrecision: Int = 64
  override val hasChaining: Boolean = true
}
