package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Mips1Assembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  commentPattern = "(;|//).*"
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 32
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 6
  override val formats = Seq("R", "I", "J")
  override val hasMultiplyDivide: Boolean = false

  def parseReg(regStr: String): Int = {
    val s = regStr.trim
    if (s.startsWith("R") || s.startsWith("r")) {
      s.substring(1).toInt
    } else {
      throw new Exception(s"Invalid Mips1 register operand: $s")
    }
  }

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return 0
    val parts = s.split(" ", 2)
    val mnemonic = parts(0).toUpperCase
    if (mnemonic == "HALT") return 1
    if (mnemonic == "ADDU") return 1
    if (mnemonic == "LW" || mnemonic == "SW") return 2
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

    if (mnemonic == "HALT") return Seq(0x3F << 26)

    val ops = parts(1).split(",")

    mnemonic match {
      case "LW" =>
        if (ops.length != 2) throw new Exception(s"LW requires 2 operands: $s")
        val rt = parseReg(ops(0))
        val addr = parseNumericOrSymbol(ops(1))
        val inst = (0x23 << 26) | (rt << 16)
        Seq(inst, addr)

      case "SW" =>
        if (ops.length != 2) throw new Exception(s"SW requires 2 operands: $s")
        val rt = parseReg(ops(0))
        val addr = parseNumericOrSymbol(ops(1))
        val inst = (0x2B << 26) | (rt << 16)
        Seq(inst, addr)

      case "ADDU" =>
        if (ops.length != 3) throw new Exception(s"ADDU requires 3 operands: $s")
        val rd = parseReg(ops(0))
        val rs = parseReg(ops(1))
        val rt = parseReg(ops(2))
        val inst = (0x00 << 26) | (rs << 21) | (rt << 16) | (rd << 11) | 0x21
        Seq(inst)

      case _ =>
        throw new Exception(s"Unknown Mips1 instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Mips1 Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
