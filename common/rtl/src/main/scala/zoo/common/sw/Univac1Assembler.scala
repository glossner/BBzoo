package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Univac1Assembler extends BaseAssembler with AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 72
  override val addressWidth: Int = 16
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 8
  override val instructionWidth: Int = 72
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

    if (mnemonic == "Q") return Seq(5 << 16)

    if (Seq("B", "H", "A", "S").contains(mnemonic)) {
      val op = mnemonic match {
        case "B" => 1
        case "H" => 2
        case "A" => 3
        case "S" => 4
      }
      val addr = parseNumericOrSymbol(parts(1))
      return Seq((op << 16) | (addr & 0xFFFF))
    }

    try {
      Seq(parseNumericOrSymbol(s))
    } catch {
      case _: Exception =>
        throw new Exception(s"Unknown Univac I instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Univac I Hex File"
    for (valWord <- values) {
      val bigVal = if ((valWord & 0xFFFF0000) != 0) {
        val op = BigInt((valWord >> 16) & 0xFFFF)
        val addr = BigInt(valWord & 0xFFFF)
        (op << 64) | addr
      } else {
        BigInt(valWord.toLong & 0xFFFFFFFFL)
      }
      out += f"$bigVal%018X"
    }
    out.mkString("\n") + "\n"
  }
}
