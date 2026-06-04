package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Ibm650Assembler extends BaseAssembler with AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 40
  override val addressWidth: Int = 16
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 8
  override val instructionWidth: Int = 40
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
      return Seq(4 << 24) // opcode 4, data_addr 0, next_addr 0
    }

    if (Seq("LD", "ADD", "ST").contains(mnemonic)) {
      val op = mnemonic match {
        case "LD"  => 1
        case "ADD" => 2
        case "ST"  => 3
      }
      val op1 = parts(1).replace(",", "").trim
      val op2 = parts(2).trim
      val data_addr = parseNumericOrSymbol(op1)
      val next_addr = parseNumericOrSymbol(op2)
      return Seq((op << 24) | ((data_addr & 0xFFF) << 12) | (next_addr & 0xFFF))
    }

    try {
      Seq(parseNumericOrSymbol(s))
    } catch {
      case _: Exception =>
        throw new Exception(s"Unknown IBM 650 instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled IBM 650 Hex File"
    for (valWord <- values) {
      val bigVal = if ((valWord & 0xFF000000) != 0) {
        val op = BigInt((valWord >> 24) & 0xFF)
        val data_addr = BigInt((valWord >> 12) & 0xFFF)
        val next_addr = BigInt(valWord & 0xFFF)
        (op << 32) | (data_addr << 16) | next_addr
      } else {
        BigInt(valWord.toLong & 0xFFFFFFFFL)
      }
      out += f"$bigVal%010X"
    }
    out.mkString("\n") + "\n"
  }
}
