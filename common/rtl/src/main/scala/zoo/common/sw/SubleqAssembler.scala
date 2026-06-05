package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class SubleqAssembler extends BaseAssembler with StackArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val stackDepth: Int = 0
  override val opCodeWidth: Int = 0
  override val formats = Seq("Subleq")
  override val hasMultiplyDivide: Boolean = false
  commentPattern = "(;|//).*"

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim
    if (s.isEmpty) return 0
    val parts = s.split("\\s+", 2)
    val op = parts(0).toUpperCase
    if (op == "SUBLEQ") return 3
    if (op == "HLT" || op == "HALT") return 3
    1
  }

  def parseNumericOrSymbol(valStr: String): Int = {
    val s = valStr.trim
    val key = symbols.keys.find(_.equalsIgnoreCase(s))
    if (key.isDefined) {
      symbols(key.get)
    } else {
      parseNumeric(s)
    }
  }

  override def assembleInstruction(line: String): Seq[Int] = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return Seq()

    val parts = s.split(" ", 2)
    val op = parts(0).toUpperCase

    if (op == "HLT" || op == "HALT") {
      return Seq(0, 0, 0xFFFFFFFF)
    }

    if (op == "SUBLEQ") {
      if (parts.length < 2) {
        throw new Exception(s"Missing operands for SUBLEQ: $line")
      }
      val ops = parts(1).split(",")
      if (ops.length != 3) {
        throw new Exception(s"SUBLEQ requires A, B, C operands: $line")
      }
      val a = parseNumericOrSymbol(ops(0))
      val b = parseNumericOrSymbol(ops(1))
      val c = parseNumericOrSymbol(ops(2))
      return Seq(a, b, c)
    }

    throw new Exception(s"Unknown SUBLEQ instruction: $line")
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled SUBLEQ Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
