package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class CdcStar100Assembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 8
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 4
  override val formats = Seq("Single")
  override val hasMultiplyDivide: Boolean = false
  commentPattern = "(#|//).*"

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim
    if (s.isEmpty) return 0
    1
  }

  def parseNumericOrSymbol(valStr: String): Int = {
    val s = valStr.trim
    val key = symbols.keys.find(_.equalsIgnoreCase(s))
    if (key.isDefined) {
      symbols(key.get)
    } else {
      parseNumeric(s)
    }
  }

  def parseReg(regStr: String): Int = {
    val s = regStr.trim.toUpperCase
    if (s.startsWith("R")) {
      s.substring(1).toInt
    } else {
      throw new Exception(s"Invalid register: $regStr")
    }
  }

  override def assembleInstruction(line: String): Seq[Int] = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return Seq()

    val parts = s.split(" ", 2)
    val op = parts(0).toUpperCase

    if (op == "HLT" || op == "HALT") {
      return Seq(3 << 28)
    }

    if (op == "JMP") {
      val target = parseNumericOrSymbol(parts(1))
      return Seq((4 << 28) | (target & 0xFFFF))
    }

    if (op == "LD_CU") {
      val operands = parts(1).split(",")
      val rd = parseReg(operands(0))
      val target = parseNumericOrSymbol(operands(1))
      return Seq((5 << 28) | (rd << 24) | (target & 0xFFFF))
    }

    if (op == "VADD" || op == "VSUB") {
      val operands = parts(1).split(",")
      val rc = parseReg(operands(0))
      val ra = parseReg(operands(1))
      val rb = parseReg(operands(2))
      val rlen = parseReg(operands(3))
      val opCode = op match {
        case "VADD" => 1
        case "VSUB" => 2
      }
      return Seq((opCode << 28) | (rc << 24) | (ra << 20) | (rb << 16) | (rlen << 12))
    }

    throw new Exception(s"Unknown CDC STAR-100 instruction: $line")
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled CDC STAR-100 Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
