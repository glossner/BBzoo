package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class ManchesterAssembler extends BaseAssembler with AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 13
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 3
  override val instructionWidth: Int = 32
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

    if (mnemonic == "STP") return Seq(7 << 13)

    if (Seq("JMP", "JPR", "LDN", "STO", "SUB").contains(mnemonic)) {
      val op = mnemonic match {
        case "JMP" => 0
        case "JPR" => 1
        case "LDN" => 2
        case "STO" => 3
        case "SUB" => 4
      }
      val addr = parseNumericOrSymbol(parts(1))
      return Seq((op << 13) | (addr & 0x1FFF))
    }

    try {
      Seq(parseNumericOrSymbol(s))
    } catch {
      case _: Exception =>
        throw new Exception(s"Unknown Manchester instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Manchester Hex File"
    for (valWord <- values) {
      out += f"${valWord & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
