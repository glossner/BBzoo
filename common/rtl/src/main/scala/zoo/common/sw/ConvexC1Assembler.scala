package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class ConvexC1Assembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
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

  def parseVecReg(regStr: String): Int = {
    val s = regStr.trim.toUpperCase
    if (s.startsWith("V")) {
      s.substring(1).toInt
    } else {
      throw new Exception(s"Invalid vector register: $regStr")
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

    if (op == "SETVL") {
      val rs = parseReg(parts(1).trim)
      return Seq((6 << 28) | (rs << 24))
    }

    val operands = parts(1).split(",")
    if (op == "VLD") {
      val vi = parseVecReg(operands(0))
      val rs = parseReg(operands(1))
      return Seq((7 << 28) | (vi << 24) | (rs << 20))
    } else if (op == "VST") {
      val vi = parseVecReg(operands(0))
      val rs = parseReg(operands(1))
      return Seq((8 << 28) | (vi << 24) | (rs << 20))
    } else {
      val vk = parseVecReg(operands(0))
      val vi = parseVecReg(operands(1))
      val vj = parseVecReg(operands(2))
      val opCode = op match {
        case "VADD" => 1
        case "VSUB" => 2
      }
      return Seq((opCode << 28) | (vk << 24) | (vi << 20) | (vj << 16))
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Convex C1 Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
