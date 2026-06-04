package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Tms32010Assembler extends BaseAssembler with AccumulatorArchitecture with VariableLengthFormat with FixedPointOperations {
  commentPattern = "(;|//).*"
  override val wordWidth: Int = 16
  override val addressWidth: Int = 16
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 8
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = true

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return 0
    val parts = s.split(" ", 2)
    val mnemonic = parts(0).toUpperCase
    if (mnemonic == "HALT" || mnemonic == "HLT") return 1
    if (mnemonic == "LAC" || mnemonic == "ADD" || mnemonic == "SACL") return 2
    1
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

    if (mnemonic == "HALT" || mnemonic == "HLT") return Seq(0x00 << 8)

    val ops = parts(1)

    mnemonic match {
      case "LAC" =>
        val addr = parseNumericOrSymbol(ops)
        Seq(0x01 << 8, addr)

      case "ADD" =>
        val addr = parseNumericOrSymbol(ops)
        Seq(0x02 << 8, addr)

      case "SACL" =>
        val addr = parseNumericOrSymbol(ops)
        Seq(0x03 << 8, addr)

      case _ =>
        throw new Exception(s"Unknown TMS32010 instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled TMS32010 Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFF}%04X"
    }
    out.mkString("\n") + "\n"
  }
}
