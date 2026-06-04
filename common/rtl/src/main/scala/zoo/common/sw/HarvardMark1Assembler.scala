package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class HarvardMark1Assembler extends BaseAssembler with GeneralRegisterArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 64
  override val addressWidth: Int = 8
  override val numGPRs: Int = 72
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 8
  override val instructionWidth: Int = 64
  override val hasMultiplyDivide: Boolean = false

  override def getArchInstructionSize(line: String): Int = 1

  def parseRegister(regStr: String): Int = {
    val s = regStr.trim.toUpperCase
    if (s.startsWith("R")) {
      s.substring(1).toInt
    } else {
      throw new Exception(s"Invalid Harvard Mark I register: $regStr")
    }
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

    if (mnemonic == "HLT") {
      return Seq(4 << 16)
    }

    if (parts.length < 2) {
      throw new Exception(s"Missing operands for instruction: $s")
    }

    val ops = parts(1).split(",")
    val srcStr = ops(0).trim
    val dstStr = ops(1).trim

    val op = mnemonic match {
      case "MOV" => 1
      case "ADD" => 2
      case "SUB" => 3
      case _     => throw new Exception(s"Unknown Harvard Mark I instruction: $s")
    }

    val src = parseRegister(srcStr)
    val dst = parseRegister(dstStr)

    Seq((op << 16) | (src << 8) | dst)
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Harvard Mark I Hex File"
    for (valWord <- values) {
      // Reconstruct 64-bit value: (op << 16) | (src << 8) | dst
      val longVal = if ((valWord & 0xFF0000) != 0) {
        val op = ((valWord >> 16) & 0xFF).toLong
        val src = ((valWord >> 8) & 0xFF).toLong
        val dst = (valWord & 0xFF).toLong
        (op << 16) | (src << 8) | dst
      } else {
        valWord.toLong & 0xFFFFFFFFFFFFFFFFL
      }
      out += f"$longVal%016X"
    }
    out.mkString("\n") + "\n"
  }
}
