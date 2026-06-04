package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Motorola6800Assembler extends BaseAssembler with AccumulatorArchitecture with VariableLengthFormat with FixedPointOperations {
  commentPattern = "(;|//).*"
  override val wordWidth: Int = 8
  override val addressWidth: Int = 16
  override val hasLinkBit: Boolean = true
  override val opCodeWidth: Int = 8
  override val hasMultiplyDivide: Boolean = false
  override val formats = Seq("Single", "Double", "Triple")

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return 0
    val parts = s.split(" ", 2)
    val mnemonic = parts(0).toUpperCase
    if (mnemonic == "WAI") 1 else 3
  }

  def parseNumericOrSymbol(valStr: String): Int = {
    val s = valStr.trim
    if (symbols.contains(s)) {
      symbols(s)
    } else {
      parseNumeric(s)
    }
  }

  override def assembleInstruction(line: String): Seq[Int] = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return Seq()

    val parts = s.split(" ", 2)
    val mnemonic = parts(0).toUpperCase

    if (mnemonic == "WAI") return Seq(0x3E)

    if (parts.length < 2) {
      throw new Exception(s"Missing operand for instruction: $s")
    }

    val operandStr = parts(1).trim
    val value = parseNumericOrSymbol(operandStr)

    mnemonic match {
      case "LDAA" => Seq(0xB6, (value >> 8) & 0xFF, value & 0xFF)
      case "STAA" => Seq(0xB7, (value >> 8) & 0xFF, value & 0xFF)
      case "ADDA" => Seq(0xBB, (value >> 8) & 0xFF, value & 0xFF)
      case _ =>
        throw new Exception(s"Unknown Motorola 6800 instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Motorola 6800 Hex File"
    for (v <- values) {
      out += f"${v & 0xFF}%02X"
    }
    out.mkString("\n") + "\n"
  }
}
