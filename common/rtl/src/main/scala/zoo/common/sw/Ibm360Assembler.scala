package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Ibm360Assembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth = 32
  override val addressWidth = 24
  override val numGPRs = 16
  override val numFPRs = 4
  override val opCodeWidth = 8
  override val formats = Seq("RR", "RX")
  override val hasMultiplyDivide = true

  def cleanInstruction(line: String): Seq[String] = {
    line.replace(',', ' ').replace('(', ' ').replace(')', ' ').trim.split("\\s+").toSeq
  }

  override def getArchInstructionSize(line: String): Int = {
    val parts = cleanInstruction(line)
    if (parts.isEmpty) return 0
    val mnemonic = parts(0).toUpperCase
    if (mnemonic == "AR") 2
    else if (Seq("L", "ST", "BC").contains(mnemonic)) 4
    else 0
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
    val parts = cleanInstruction(line)
    if (parts.isEmpty) return Seq()

    val mnemonic = parts(0).toUpperCase

    if (mnemonic == "AR") {
      val r1 = parseNumericOrSymbol(parts(1))
      val r2 = parseNumericOrSymbol(parts(2))
      Seq(0x1A, ((r1 & 0xF) << 4) | (r2 & 0xF))
    } else if (Seq("L", "ST", "BC").contains(mnemonic)) {
      val opcodes = Map("L" -> 0x58, "ST" -> 0x50, "BC" -> 0x47)
      val opcode = opcodes(mnemonic)
      val r1 = parseNumericOrSymbol(parts(1))
      val d2 = parseNumericOrSymbol(parts(2))

      var x2 = 0
      var b2 = 0
      if (parts.length > 3) {
        x2 = parseNumericOrSymbol(parts(3))
      }
      if (parts.length > 4) {
        b2 = parseNumericOrSymbol(parts(4))
      }

      val byte1 = ((r1 & 0xF) << 4) | (x2 & 0xF)
      val byte2 = ((b2 & 0xF) << 4) | ((d2 >> 8) & 0xF)
      val byte3 = d2 & 0xFF
      Seq(opcode, byte1, byte2, byte3)
    } else {
      try {
        Seq(parseNumericOrSymbol(parts(0)) & 0xFF)
      } catch {
        case _: Exception =>
          throw new Exception(s"Unknown IBM 360 instruction/operand: $line")
      }
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled IBM System/360 Hex File"
    for (valByte <- values) {
      out += f"${valByte & 0xFF}%02X"
    }
    out.mkString("\n") + "\n"
  }
}
