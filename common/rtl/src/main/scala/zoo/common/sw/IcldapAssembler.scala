package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class IcldapAssembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
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
    if (mnemonic == "CLR_CARRY") return 1
    if (mnemonic.startsWith("LD_PE_BIT") || mnemonic.startsWith("ADD_PE_BIT") || mnemonic.startsWith("ST_PE_BIT")) return 1
    if (mnemonic == "LD_CU" || mnemonic == "ST_CU") return 2
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
    if (mnemonic == "CLR_CARRY") return Seq(0x6000)

    if (mnemonic == "LD_PE_BIT" || mnemonic == "ADD_PE_BIT" || mnemonic == "ST_PE_BIT") {
      val bit = parseNumeric(parts(1))
      val op = mnemonic match {
        case "LD_PE_BIT"  => 0x3000
        case "ADD_PE_BIT" => 0x4000
        case "ST_PE_BIT"  => 0x5000
      }
      return Seq(op | (bit & 0xF))
    }

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

      case _ =>
        throw new Exception(s"Unknown ICL DAP instruction: $line")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled ICL DAP Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFF}%04X"
    }
    out.mkString("\n") + "\n"
  }
}
