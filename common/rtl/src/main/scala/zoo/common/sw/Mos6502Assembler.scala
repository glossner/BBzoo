package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Mos6502Assembler extends BaseAssembler with AccumulatorArchitecture with VariableLengthFormat with FixedPointOperations {
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
    if (mnemonic == "BRK" || mnemonic == "CLC") return 1
    if (parts.length < 2) return 1
    val op = parts(1).trim
    if (op.startsWith("#")) 2 else 3
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

    if (mnemonic == "BRK") return Seq(0x00)
    if (mnemonic == "CLC") return Seq(0x18)

    if (parts.length < 2) {
      throw new Exception(s"Missing operand for instruction: $s")
    }

    val operandStr = parts(1).trim
    val isImm = operandStr.startsWith("#")
    val valStr = if (isImm) operandStr.substring(1) else operandStr

    val value = parseNumericOrSymbol(valStr)

    mnemonic match {
      case "LDA" =>
        if (isImm) {
          Seq(0xA9, value & 0xFF)
        } else {
          Seq(0xAD, value & 0xFF, (value >> 8) & 0xFF)
        }
      case "STA" =>
        if (isImm) {
          throw new Exception("STA does not support immediate addressing mode")
        } else {
          Seq(0x8D, value & 0xFF, (value >> 8) & 0xFF)
        }
      case "ADC" =>
        if (isImm) {
          Seq(0x69, value & 0xFF)
        } else {
          Seq(0x6D, value & 0xFF, (value >> 8) & 0xFF)
        }
      case _ =>
        throw new Exception(s"Unknown MOS 6502 instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled MOS 6502 Hex File"
    for (v <- values) {
      out += f"${v & 0xFF}%02X"
    }
    out.mkString("\n") + "\n"
  }
}
