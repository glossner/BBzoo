package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Illiac4Assembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 16
  override val addressWidth: Int = 16
  override val numGPRs: Int = 4
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
    if (mnemonic == "LD_PE" || mnemonic == "ST_PE" || mnemonic == "ADD_PE" || mnemonic == "ROUTE_PE_L" || mnemonic == "ROUTE_PE_R" || mnemonic == "ADD_PE_REG") return 1
    if (mnemonic == "LD_CU" || mnemonic == "ST_CU" || mnemonic == "JMP_CU" || mnemonic == "JNZ_CU") return 2
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
    if (mnemonic == "LD_PE") return Seq(0x7000)
    if (mnemonic == "ST_PE") return Seq(0x8000)
    if (mnemonic == "ADD_PE") return Seq(0x9000)
    if (mnemonic == "ROUTE_PE_L") return Seq(0xA000)
    if (mnemonic == "ROUTE_PE_R") return Seq(0xB000)
    if (mnemonic == "ADD_PE_REG") return Seq(0xC000)

    val ops = parts(1).split(",")

    mnemonic match {
      case "LD_CU" =>
        val rd = parseReg(ops(0))
        val addr = parseNumericOrSymbol(ops(1))
        Seq(0x1000 | (rd << 8), addr)

      case "ST_CU" =>
        val rs = parseReg(ops(0))
        val addr = parseNumericOrSymbol(ops(1))
        Seq(0x2000 | (rs << 8), addr)

      case "LDI_CU" =>
        val rd = parseReg(ops(0))
        val rs = parseReg(ops(1))
        Seq(0x3000 | (rd << 8) | (rs << 4))

      case "STI_CU" =>
        val rs = parseReg(ops(0))
        val rd = parseReg(ops(1))
        Seq(0x4000 | (rs << 8) | (rd << 4))

      case "JNZ_CU" =>
        val rd = parseReg(ops(0))
        val addr = parseNumericOrSymbol(ops(1))
        Seq(0x5000 | (rd << 8), addr)

      case "JMP_CU" =>
        val addr = parseNumericOrSymbol(ops(0))
        Seq(0x6000, addr)

      case _ =>
        throw new Exception(s"Unknown ILLIAC IV instruction: $line")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled ILLIAC IV Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFF}%04X"
    }
    out.mkString("\n") + "\n"
  }
}
