package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Ibm6150Assembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
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
      throw new Exception(s"Invalid register operand: $s")
    }
  }

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return 0
    val parts = s.split(" ", 2)
    val mnemonic = parts(0).toUpperCase
    if (mnemonic == "HALT") return 1
    if (mnemonic == "A") return 1
    if (mnemonic == "L" || mnemonic == "ST") return 2
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

    if (mnemonic == "HALT") return Seq(0)

    val ops = parts(1).split(",")
    if (ops.length != 2) {
      throw new Exception(s"Two operand instruction requires exactly 2 operands: $s")
    }

    val op0 = ops(0).trim
    val op1 = ops(1).trim

    mnemonic match {
      case "L" =>
        val rx = parseReg(op0)
        val addr = parseNumericOrSymbol(op1)
        val inst = (0x80 << 24) | (rx << 20)
        Seq(inst, addr)

      case "ST" =>
        val rx = parseReg(op0)
        val addr = parseNumericOrSymbol(op1)
        val inst = (0x90 << 24) | (rx << 20)
        Seq(inst, addr)

      case "A" =>
        val rx = parseReg(op0)
        val ry = parseReg(op1)
        val inst = (0xA0 << 24) | (rx << 20) | (ry << 16)
        Seq(inst)

      case _ =>
        throw new Exception(s"Unknown IBM 6150 instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled IBM 6150 Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
