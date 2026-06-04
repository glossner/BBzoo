package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Arm1Assembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  commentPattern = "(;|//).*"
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 16
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 8
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = false

  def parseReg(regStr: String): Int = {
    val s = regStr.trim
    if (s.startsWith("R") || s.startsWith("r")) {
      s.substring(1).toInt
    } else {
      throw new Exception(s"Invalid ARM1 register operand: $s")
    }
  }

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return 0
    val parts = s.split(" ", 2)
    val mnemonic = parts(0).toUpperCase
    if (mnemonic == "HALT") return 1
    if (mnemonic == "ADD") return 1
    if (mnemonic == "LDR" || mnemonic == "STR") return 2
    1
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

    if (mnemonic == "HALT") return Seq((0xE << 28) | (0x0F << 20))

    val ops = parts(1).split(",")

    mnemonic match {
      case "LDR" =>
        if (ops.length != 2) throw new Exception(s"LDR requires 2 operands: $s")
        val rd = parseReg(ops(0))
        val addr = parseNumericOrSymbol(ops(1))
        val inst = (0xE << 28) | (0x04 << 20) | (rd << 12)
        Seq(inst, addr)

      case "STR" =>
        if (ops.length != 2) throw new Exception(s"STR requires 2 operands: $s")
        val rd = parseReg(ops(0))
        val addr = parseNumericOrSymbol(ops(1))
        val inst = (0xE << 28) | (0x05 << 20) | (rd << 12)
        Seq(inst, addr)

      case "ADD" =>
        if (ops.length != 3) throw new Exception(s"ADD requires 3 operands: $s")
        val rd = parseReg(ops(0))
        val rn = parseReg(ops(1))
        val rm = parseReg(ops(2))
        val inst = (0xE << 28) | (0x00 << 20) | (rn << 16) | (rd << 12) | rm
        Seq(inst)

      case _ =>
        throw new Exception(s"Unknown ARM1 instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled ARM1 Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
