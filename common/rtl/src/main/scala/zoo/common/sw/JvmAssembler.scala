package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class JvmAssembler extends BaseAssembler with StackArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val stackDepth: Int = 16
  override val opCodeWidth: Int = 8
  override val formats = Seq("Stack")
  override val hasMultiplyDivide: Boolean = false
  commentPattern = "(;|//).*"

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim
    if (s.isEmpty) return 0
    val parts = s.split("\\s+", 2)
    val op = parts(0).toUpperCase
    if (op == "HLT" || op == "HALT") return 1
    if (op == "IADD") return 1
    if (op == "ILOAD" || op == "ISTORE") return 2
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
      return Seq(0xFF << 24)
    }

    if (op == "IADD") {
      return Seq(0x60 << 24)
    }

    val target = parseNumericOrSymbol(parts(1))

    if (op == "ILOAD") {
      return Seq(0x15 << 24, target)
    }

    if (op == "ISTORE") {
      return Seq(0x36 << 24, target)
    }

    throw new Exception(s"Unknown JVM instruction: $line")
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled JVM Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
