package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Intel8080aAssembler extends BaseAssembler with AccumulatorArchitecture with VariableLengthFormat with FixedPointOperations {
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
    if (mnemonic == "HLT" || mnemonic == "MOV" || mnemonic == "ADD") 1 else 3
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

    if (mnemonic == "HLT") return Seq(0x76)

    if (parts.length < 2) {
      throw new Exception(s"Missing operand for instruction: $s")
    }

    val operandStr = parts(1).trim

    mnemonic match {
      case "MOV" =>
        val cleanOps = operandStr.replace(" ", "").toUpperCase
        if (cleanOps == "B,A") {
          Seq(0x47)
        } else {
          throw new Exception(s"Unsupported MOV operands: $operandStr")
        }
      case "ADD" =>
        if (operandStr.toUpperCase == "B") {
          Seq(0x80)
        } else {
          throw new Exception(s"Unsupported ADD operand: $operandStr")
        }
      case "LDA" =>
        val value = parseNumericOrSymbol(operandStr)
        Seq(0x3A, value & 0xFF, (value >> 8) & 0xFF)
      case "STA" =>
        val value = parseNumericOrSymbol(operandStr)
        Seq(0x32, value & 0xFF, (value >> 8) & 0xFF)
      case _ =>
        throw new Exception(s"Unknown Intel 8080A instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Intel 8080A Hex File"
    for (v <- values) {
      out += f"${v & 0xFF}%02X"
    }
    out.mkString("\n") + "\n"
  }
}
