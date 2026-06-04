package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class CambridgeedsacAssembler extends BaseAssembler with AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 17
  override val addressWidth: Int = 10
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 5
  override val instructionWidth: Int = 17
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

    if (mnemonic == "Z") return Seq(5 << 10)

    if (Seq("A", "S", "T", "U").contains(mnemonic)) {
      val op = mnemonic match {
        case "A" => 1
        case "S" => 2
        case "T" => 3
        case "U" => 4
      }
      val addr = parseNumericOrSymbol(parts(1))
      return Seq((op << 10) | (addr & 0x3FF))
    }

    try {
      Seq(parseNumericOrSymbol(s))
    } catch {
      case _: Exception =>
        throw new Exception(s"Unknown Cambridgeedsac instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Cambridgeedsac Hex File"
    for (valWord <- values) {
      val bigVal = BigInt(valWord.toLong & 0x1FFFFL)
      out += f"$bigVal%05X"
    }
    out.mkString("\n") + "\n"
  }
}
