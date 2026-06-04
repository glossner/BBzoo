package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Amd2901Assembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 16
  override val addressWidth: Int = 16
  override val numGPRs: Int = 16
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 4
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = false

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return 0
    val parts = s.split(" ", 2)
    val mnemonic = parts(0).toUpperCase
    if (mnemonic == "HALT" || mnemonic == "HLT") return 1
    if (mnemonic == "LD" || mnemonic == "ST" || mnemonic == "JMP" || mnemonic == "JNZ") return 2
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
    val mnemonic = parts(0).toUpperCase

    if (mnemonic == "HALT" || mnemonic == "HLT") return Seq(0x0000)

    val ops = parts(1).split(",")

    mnemonic match {
      case "LD" =>
        val rd = parseReg(ops(0))
        val addr = parseNumericOrSymbol(ops(1))
        Seq(0x1000 | (rd << 8), addr)

      case "ST" =>
        val rs = parseReg(ops(0))
        val addr = parseNumericOrSymbol(ops(1))
        Seq(0x2000 | (rs << 8), addr)

      case "ADD" =>
        val rd = parseReg(ops(0))
        val ra = parseReg(ops(1))
        val rb = parseReg(ops(2))
        Seq(0x3000 | (rd << 8) | (ra << 4) | rb)

      case "SUB" =>
        val rd = parseReg(ops(0))
        val ra = parseReg(ops(1))
        val rb = parseReg(ops(2))
        Seq(0x4000 | (rd << 8) | (ra << 4) | rb)

      case "AND" =>
        val rd = parseReg(ops(0))
        val ra = parseReg(ops(1))
        val rb = parseReg(ops(2))
        Seq(0x5000 | (rd << 8) | (ra << 4) | rb)

      case "OR" =>
        val rd = parseReg(ops(0))
        val ra = parseReg(ops(1))
        val rb = parseReg(ops(2))
        Seq(0x6000 | (rd << 8) | (ra << 4) | rb)

      case "JNZ" =>
        val rd = parseReg(ops(0))
        val addr = parseNumericOrSymbol(ops(1))
        Seq(0x7000 | (rd << 8), addr)

      case "JMP" =>
        val addr = parseNumericOrSymbol(ops(0))
        Seq(0x8000, addr)

      case "LDI" =>
        val rd = parseReg(ops(0))
        val rs = parseReg(ops(1))
        Seq(0x9000 | (rd << 8) | (rs << 4))

      case "STI" =>
        val rs = parseReg(ops(0))
        val rd = parseReg(ops(1))
        Seq(0xA000 | (rs << 8) | (rd << 4))

      case _ =>
        throw new Exception(s"Unknown AMD 2901 instruction: $line")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled AMD 2901 Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFF}%04X"
    }
    out.mkString("\n") + "\n"
  }
}
