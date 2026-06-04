package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class ZuseZ1Assembler extends BaseAssembler with GeneralRegisterArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 22
  override val addressWidth: Int = 16
  override val numGPRs: Int = 2
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 8
  override val instructionWidth: Int = 22
  override val hasMultiplyDivide: Boolean = false

  override def getArchInstructionSize(line: String): Int = 1

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

    val parts = s.split(" ")
    val mnemonic = parts(0).toUpperCase

    if (mnemonic == "HLT") return Seq(6 << 16)
    if (mnemonic == "ADD") return Seq(3 << 16)
    if (mnemonic == "SUB") return Seq(4 << 16)
    if (mnemonic == "MOV") return Seq(5 << 16)

    if (Seq("PR", "PS").contains(mnemonic)) {
      val op = if (mnemonic == "PR") 1 else 2
      val addr = parseNumericOrSymbol(parts(1))
      return Seq((op << 16) | (addr & 0xFFFF))
    }

    try {
      Seq(parseNumericOrSymbol(s))
    } catch {
      case _: Exception =>
        throw new Exception(s"Unknown Zuse Z1 instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Zuse Z1 Hex File"
    for (valWord <- values) {
      out += f"${valWord & 0x3FFFFF}%06X"
    }
    out.mkString("\n") + "\n"
  }
}
