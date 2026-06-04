package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Cdc6600ppuAssembler extends BaseAssembler with AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 12
  override val addressWidth: Int = 12
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 6
  override val instructionWidth: Int = 12
  override val hasMultiplyDivide: Boolean = false

  override def getArchInstructionSize(line: String): Int = 1

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

    val parts = s.split(" ")
    val mnemonic = parts(0).toUpperCase

    if (mnemonic == "HLT") {
      return Seq(5 << 6)
    }

    if (Seq("LD", "ADD", "ST", "SUB").contains(mnemonic)) {
      val op = mnemonic match {
        case "LD"  => 1
        case "ADD" => 2
        case "ST"  => 3
        case "SUB" => 4
      }
      val addr = parseNumericOrSymbol(parts(1))
      return Seq((op << 6) | (addr & 0x3F))
    }

    try {
      Seq(parseNumericOrSymbol(s))
    } catch {
      case _: Exception =>
        throw new Exception(s"Unknown CDC 6600 PPU instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled CDC 6600 PPU Hex File"
    for (valWord <- values) {
      out += f"${valWord & 0xFFF}%03X"
    }
    out.mkString("\n") + "\n"
  }
}
