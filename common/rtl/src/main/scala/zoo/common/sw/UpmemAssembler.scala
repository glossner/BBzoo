package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class UpmemAssembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 24
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
      throw new Exception(s"Invalid UPMEM register: $regStr")
    }
  }

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

    if (op == "LW") {
      if (ops.length != 3) throw new Exception(s"LW instruction requires 3 operands: $line")
      val rd = parseReg(ops(0))
      val rs = parseReg(ops(1))
      val offset = parseNumericOrSymbol(ops(2))
      Seq((0x02 << 26) | (rd << 21) | (rs << 16) | (offset & 0xFFFF))
    } else if (op == "SW") {
      if (ops.length != 3) throw new Exception(s"SW instruction requires 3 operands: $line")
      val rd = parseReg(ops(0))
      val rs = parseReg(ops(1))
      val offset = parseNumericOrSymbol(ops(2))
      Seq((0x03 << 26) | (rd << 21) | (rs << 16) | (offset & 0xFFFF))
    } else if (op == "ADD") {
      if (ops.length != 3) throw new Exception(s"ADD instruction requires 3 operands: $line")
      val rd = parseReg(ops(0))
      val rs1 = parseReg(ops(1))
      val rs2 = parseReg(ops(2))
      Seq((0x01 << 26) | (rd << 21) | (rs1 << 16) | (rs2 << 11))
    } else if (op == "SUB") {
      if (ops.length != 3) throw new Exception(s"SUB instruction requires 3 operands: $line")
      val rd = parseReg(ops(0))
      val rs1 = parseReg(ops(1))
      val rs2 = parseReg(ops(2))
      Seq((0x04 << 26) | (rd << 21) | (rs1 << 16) | (rs2 << 11))
    } else if (op == "ADDI") {
      if (ops.length != 3) throw new Exception(s"ADDI instruction requires 3 operands: $line")
      val rd = parseReg(ops(0))
      val rs1 = parseReg(ops(1))
      val imm = parseNumericOrSymbol(ops(2))
      Seq((0x05 << 26) | (rd << 21) | (rs1 << 16) | (imm & 0xFFFF))
    } else if (op == "BNE") {
      if (ops.length != 3) throw new Exception(s"BNE instruction requires 3 operands: $line")
      val rs1 = parseReg(ops(0))
      val rs2 = parseReg(ops(1))
      val target = parseNumericOrSymbol(ops(2))
      val disp = target - (pc + 1)
      Seq((0x06 << 26) | (rs1 << 21) | (rs2 << 16) | (disp & 0xFFFF))
    } else {
      throw new Exception(s"Unknown UPMEM instruction: $line")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled UPMEM Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
