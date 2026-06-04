package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Radeonr100Assembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  commentPattern = "(;|//).*"
  override val wordWidth: Int = 32
  override val addressWidth: Int = 16
  override val numGPRs: Int = 4
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 8
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = true

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return 0
    val parts = s.split(" ", 2)
    val mnemonic = parts(0).toUpperCase
    if (mnemonic == "HALT" || mnemonic == "TAPESTRY") return 1
    if (mnemonic == "LD" || mnemonic == "ST") return 2
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

    if (mnemonic == "HALT") return Seq(0x00 << 24)

    val ops = parts(1).split(",")

    mnemonic match {
      case "LD" =>
        val reg = ops(0).trim.toUpperCase
        val addr = parseNumericOrSymbol(ops(1))
        val regIdx = reg.substring(1).toInt
        Seq((0x10 << 24) | (regIdx << 22), addr)

      case "ST" =>
        val reg = ops(0).trim.toUpperCase
        val addr = parseNumericOrSymbol(ops(1))
        val regIdx = reg.substring(1).toInt
        Seq((0x30 << 24) | (regIdx << 22), addr)

      case "TAPESTRY" =>
        val dest = ops(0).trim.toUpperCase
        val srcA = ops(1).trim.toUpperCase
        val srcB = ops(2).trim.toUpperCase
        val srcC = ops(3).trim.toUpperCase
        val destIdx = dest.substring(1).toInt
        val srcAIdx = srcA.substring(1).toInt
        val srcBIdx = srcB.substring(1).toInt
        val srcCIdx = srcC.substring(1).toInt
        Seq((0x20 << 24) | (destIdx << 22) | (srcAIdx << 18) | (srcBIdx << 14) | (srcCIdx << 10))

      case _ =>
        throw new Exception(s"Unknown Radeonr100 instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Radeonr100 Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
