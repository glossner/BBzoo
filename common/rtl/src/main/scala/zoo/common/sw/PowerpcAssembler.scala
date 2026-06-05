package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class PowerpcAssembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 32
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 6
  override val formats = Seq("R", "I")
  override val hasMultiplyDivide: Boolean = false
  commentPattern = "(;|//).*"

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim
    if (s.isEmpty) return 0
    val parts = s.split("\\s+", 2)
    val op = parts(0).toUpperCase
    if (op == "HLT" || op == "HALT") return 1
    if (op == "ADD") return 1
    if (op == "LWZ" || op == "STW") return 2
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
      throw new Exception(s"Invalid PowerPC register: $regStr")
    }
  }

  override def assembleInstruction(line: String): Seq[Int] = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return Seq()

    val parts = s.split(" ", 2)
    val op = parts(0).toUpperCase

    if (op == "HLT" || op == "HALT") {
      return Seq(0x3F << 26)
    }

    val operands = parts(1).split(",")

    if (op == "LWZ") {
      val rd = parseReg(operands(0))
      val addr = parseNumericOrSymbol(operands(1))
      val inst = (0x02 << 26) | (rd << 16)
      return Seq(inst, addr)
    }

    if (op == "STW") {
      val rs = parseReg(operands(0))
      val addr = parseNumericOrSymbol(operands(1))
      val inst = (0x03 << 26) | (rs << 16)
      return Seq(inst, addr)
    }

    if (op == "ADD") {
      val rd = parseReg(operands(0))
      val rs1 = parseReg(operands(1))
      val rs2 = parseReg(operands(2))
      val inst = (0x01 << 26) | (rd << 21) | (rs1 << 16) | (rs2 << 11)
      return Seq(inst)
    }

    throw new Exception(s"Unknown PowerPC instruction: $line")
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled PowerPC Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
