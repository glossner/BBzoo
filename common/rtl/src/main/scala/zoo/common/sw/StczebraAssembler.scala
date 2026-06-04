package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class StczebraAssembler extends BaseAssembler with AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 33
  override val addressWidth: Int = 16
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 15
  override val instructionWidth: Int = 33
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
      return Seq(4 << 24)
    }

    if (Seq("LD", "ADD", "ST").contains(mnemonic)) {
      val op = mnemonic match {
        case "LD"  => 1
        case "ADD" => 2
        case "ST"  => 3
      }
      val addr = parseNumericOrSymbol(parts(1))
      return Seq((op << 24) | (addr & 0xFFFF))
    }

    try {
      Seq(parseNumericOrSymbol(s))
    } catch {
      case _: Exception =>
        throw new Exception(s"Unknown STC ZEBRA instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled STC ZEBRA Hex File"
    for (valWord <- values) {
      val bigVal = if ((valWord & 0xFF000000) != 0) {
        val op = (valWord >> 24) & 0xFF
        val addr = valWord & 0xFFFF
        if (op == 1) { // LD
          (BigInt(1) << 32) | (BigInt(1) << 31) | (BigInt(1) << 29) | BigInt(addr << 5)
        } else if (op == 2) { // ADD
          (BigInt(1) << 31) | (BigInt(1) << 29) | BigInt(addr << 5)
        } else if (op == 3) { // ST
          (BigInt(1) << 30) | BigInt(addr << 5)
        } else { // HLT
          BigInt(1) << 28
        }
      } else {
        BigInt(valWord.toLong & 0xFFFFFFFFL)
      }
      out += f"$bigVal%09X"
    }
    out.mkString("\n") + "\n"
  }
}
