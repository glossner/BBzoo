package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Amdr600Assembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  commentPattern = "(;|//).*"
  override val wordWidth: Int = 32
  override val addressWidth: Int = 16
  override val numGPRs: Int = 4
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 8
  override val formats = Seq("Single")
  override val hasMultiplyDivide: Boolean = false

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim
    if (s.isEmpty) return 0
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
    val s = line.trim
    if (s.isEmpty) return Seq()

    var opA = 0
    var destA = 0
    var src1A = 0
    var src2A = 0
    var opB = 0
    var regB = 0
    var addrB = 0

    var partA = ""
    var partB = ""

    if (s.contains('|')) {
      val parts = s.split('|')
      partA = parts(0).trim
      partB = parts(1).trim
    } else {
      val firstWord = s.split(" ", 2)(0).toUpperCase
      if (firstWord == "LD" || firstWord == "ST") {
        partA = "NOP"
        partB = s
      } else {
        partA = s
        partB = "NOP"
      }
    }

    // Parse Slot A
    if (partA.nonEmpty) {
      val tokensA = partA.split(" ", 2)
      val mnemonicA = tokensA(0).toUpperCase
      if (mnemonicA == "HALT") {
        opA = 2
      } else if (mnemonicA == "ADD") {
        opA = 1
        val opsA = tokensA(1).split(",")
        destA = opsA(0).trim.toUpperCase.substring(1).toInt
        src1A = opsA(1).trim.toUpperCase.substring(1).toInt
        src2A = opsA(2).trim.toUpperCase.substring(1).toInt
      } else if (mnemonicA != "NOP") {
        throw new Exception(s"Unknown Slot A instruction: $partA")
      }
    }

    // Parse Slot B
    if (partB.nonEmpty) {
      val tokensB = partB.split(" ", 2)
      val mnemonicB = tokensB(0).toUpperCase
      if (mnemonicB == "LD" || mnemonicB == "ST") {
        opB = if (mnemonicB == "LD") 1 else 2
        val opsB = tokensB(1).split(",")
        regB = opsB(0).trim.toUpperCase.substring(1).toInt
        addrB = parseNumericOrSymbol(opsB(1))
      } else if (mnemonicB != "NOP") {
        throw new Exception(s"Unknown Slot B instruction: $partB")
      }
    }

    val instWord = (opA << 28) | (destA << 24) | (src1A << 20) | (src2A << 16) | (opB << 12) | (regB << 8) | (addrB & 0xFF)
    Seq(instWord)
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Amdr600 Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
