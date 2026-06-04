package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Ibm1401Assembler extends BaseAssembler with AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 36
  override val addressWidth: Int = 12
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 8
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

    if (mnemonic == "HLT") return Seq(4 << 24)

    if (Seq("MC", "A", "S").contains(mnemonic)) {
      val op = mnemonic match {
        case "MC" => 1
        case "A"  => 2
        case "S"  => 3
      }
      val op1 = parts(1).replace(",", "").trim
      val op2 = parts(2).trim
      val a_addr = parseNumericOrSymbol(op1)
      val b_addr = parseNumericOrSymbol(op2)
      return Seq((op << 24) | ((a_addr & 0xFFF) << 12) | (b_addr & 0xFFF))
    }

    try {
      Seq(parseNumericOrSymbol(s))
    } catch {
      case _: Exception =>
        throw new Exception(s"Unknown IBM 1401 instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled IBM 1401 Hex File"
    for (valWord <- values) {
      val bigVal = BigInt(valWord.toLong & 0xFFFFFFFFFL)
      out += f"$bigVal%09X"
    }
    out.mkString("\n") + "\n"
  }
}
