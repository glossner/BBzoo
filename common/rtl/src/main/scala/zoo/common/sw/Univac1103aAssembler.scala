package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Univac1103aAssembler extends BaseAssembler with AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 36
  override val addressWidth: Int = 15
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 6
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

    if (mnemonic == "HLT") {
      return Seq(14 << 24)
    }

    if (Seq("TP", "ADD", "SUB").contains(mnemonic)) {
      val op = mnemonic match {
        case "TP"  => 11
        case "ADD" => 12
        case "SUB" => 13
      }
      val uStr = parts(1).replace(",", "").trim
      val vStr = parts(2).trim
      val u = parseNumericOrSymbol(uStr)
      val v = parseNumericOrSymbol(vStr)
      return Seq((op << 24) | ((u & 0xFFF) << 12) | (v & 0xFFF))
    }

    try {
      Seq(parseNumericOrSymbol(s))
    } catch {
      case _: Exception =>
        throw new Exception(s"Unknown Univac 1103A instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Univac 1103A Hex File"
    for (valWord <- values) {
      val bigVal = if ((valWord & 0xFF000000) != 0) {
        val op = (valWord >> 24) & 0xFF
        val u = (valWord >> 12) & 0xFFF
        val v = valWord & 0xFFF
        (BigInt(op) << 30) | (BigInt(u) << 15) | BigInt(v)
      } else {
        BigInt(valWord.toLong & 0xFFFFFFFFL)
      }
      out += f"$bigVal%09X"
    }
    out.mkString("\n") + "\n"
  }
}
