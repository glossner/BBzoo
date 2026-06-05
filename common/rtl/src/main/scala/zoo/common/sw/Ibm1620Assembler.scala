package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Ibm1620Assembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 32
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 6
  override val formats = Seq("R", "I")
  override val hasMultiplyDivide: Boolean = false
  commentPattern = "(;|//).*"

  def parseReg(regStr: String): Int = {
    val s = regStr.trim
    if (s.toUpperCase.startsWith("R")) {
      s.substring(1).toInt
    } else {
      throw new Exception(s"Invalid IBM 1620 register operand: $regStr")
    }
  }

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim
    if (s.isEmpty) return 0
    val parts = s.split("\\s+", 2)
    val op = parts(0).toUpperCase
    if (op == "HLT" || op == "HALT") return 1
    if (op == "ADD") return 1
    if (op == "LOAD" || op == "STORE") return 2
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

  override def assembleInstruction(line: String): Seq[Int] = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return Seq()

    val parts = s.split(" ", 2)
    val op = parts(0).toUpperCase

    if (op == "HLT" || op == "HALT") {
      return Seq(0x3F << 26)
    }

    if (parts.length < 2) {
      throw new Exception(s"Missing operands for instruction: $line")
    }

    val ops = parts(1).split(",")

    if (op == "LOAD") {
      if (ops.length != 2) {
        throw new Exception(s"LOAD instruction requires exactly 2 operands: $line")
      }
      val rd = parseReg(ops(0))
      val addr = parseNumericOrSymbol(ops(1))
      val inst = (0x02 << 26) | (rd << 21)
      return Seq(inst, addr)
    }

    if (op == "STORE") {
      if (ops.length != 2) {
        throw new Exception(s"STORE instruction requires exactly 2 operands: $line")
      }
      val rs = parseReg(ops(0))
      val addr = parseNumericOrSymbol(ops(1))
      val inst = (0x03 << 26) | (rs << 21)
      return Seq(inst, addr)
    }

    if (op == "ADD") {
      if (ops.length != 3) {
        throw new Exception(s"ADD instruction requires exactly 3 operands: $line")
      }
      val rd = parseReg(ops(0))
      val rs1 = parseReg(ops(1))
      val rs2 = parseReg(ops(2))
      val inst = (0x22 << 26) | (rd << 21) | (rs1 << 16) | (rs2 << 11)
      return Seq(inst)
    }

    throw new Exception(s"Unknown IBM 1620 instruction: $line")
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled IBM 1620 Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
