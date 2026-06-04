package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Ibm704Assembler extends BaseAssembler with AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 36
  override val addressWidth: Int = 15
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 12
  override val instructionWidth: Int = 36
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

    if (mnemonic == "HLT") return Seq(5 << 15)

    if (Seq("LD", "ST", "ADD", "SUB").contains(mnemonic)) {
      val op = mnemonic match {
        case "LD"  => 1
        case "ST"  => 2
        case "ADD" => 3
        case "SUB" => 4
      }
      val addr = parseNumericOrSymbol(parts(1))
      return Seq((op << 15) | (addr & 0x7FFF))
    }

    try {
      Seq(parseNumericOrSymbol(s))
    } catch {
      case _: Exception =>
        throw new Exception(s"Unknown IBM 704 instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled IBM 704 Hex File"
    for (valWord <- values) {
      val cleanVal = BigInt(valWord.toLong & 0xFFFFFFFFFL)
      out += f"$cleanVal%09X"
    }
    out.mkString("\n") + "\n"
  }
}
