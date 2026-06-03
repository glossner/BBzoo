package zoo.common.architecture

/**
 * Base trait for operation execution capabilities.
 */
trait Operations

/**
 * Fixed-point (integer) and logical operations.
 * Found in almost all machines.
 */
trait FixedPointOperations extends Operations {
  val hasMultiplyDivide: Boolean
}

/**
 * Floating-point (real) operations.
 * Found in scientific processors.
 * Example: IBM System/360 Model 91, Cray-1.
 */
trait FloatingPointOperations extends Operations {
  val fpuPrecision: Int // E.g., 32, 64, or 80 bits
}

/**
 * Decimal and character string operations.
 * Found in business-oriented processors.
 * Example: IBM 1401, IBM System/360 (decimal package).
 */
trait DecimalOperations extends Operations {
  val hasDecimalPack: Boolean
}

/**
 * Vector execution operations.
 * Operations executed concurrently/pipelined over an array of data.
 * Example: Cray-1.
 */
trait VectorOperations extends Operations {
  val hasChaining: Boolean // Cray-1 style functional unit chaining
}
