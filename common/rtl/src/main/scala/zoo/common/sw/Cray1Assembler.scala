package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Cray1Assembler extends BaseAssembler with VectorArchitecture with FixedLengthFormat with VectorOperations {
  override val wordWidth = 64
  override val addressWidth = 24
  override val numGPRs = 8
  override val numFPRs = 0
  override val vectorLength = 64
  override val numVecRegs = 8
  override val opCodeWidth = 7
  override val instructionWidth = 16
  override val hasChaining = true

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
    val s = line.trim
    if (s.isEmpty) return Seq()

    if (s.toUpperCase == "HLT") {
      return Seq(0x0200)
    }

    // VL = A<j>
    val vlMatch = "^(?i)VL\\s*=\\s*A(\\d+)".r.findFirstMatchIn(s)
    if (vlMatch.isDefined) {
      val j = vlMatch.get.group(1).toInt
      return Seq((0 << 12) | (0 << 9) | (0 << 6) | (j << 3) | 0)
    }

    // A<i> = <imm>
    val liMatch = "^(?i)A(\\d+)\\s*=\\s*(.+)".r.findFirstMatchIn(s)
    if (liMatch.isDefined) {
      val h = liMatch.get.group(1).toInt
      val imm = parseNumericOrSymbol(liMatch.get.group(2))
      return Seq((4 << 12) | (h << 9) | (imm & 0x1FF))
    }

    // V<i> = mem[A<j>]
    val vloadMatch = "^(?i)V(\\d+)\\s*=\\s*mem\\s*\\[\\s*A(\\d+)\\s*\\]".r.findFirstMatchIn(s)
    if (vloadMatch.isDefined) {
      val i = vloadMatch.get.group(1).toInt
      val j = vloadMatch.get.group(2).toInt
      return Seq((2 << 12) | (0 << 9) | (i << 6) | (j << 3) | 0)
    }

    // mem[A<j>] = V<i>
    val vstoreMatch = "^(?i)mem\\s*\\[\\s*A(\\d+)\\s*\\]\\s*=\\s*V(\\d+)".r.findFirstMatchIn(s)
    if (vstoreMatch.isDefined) {
      val j = vstoreMatch.get.group(1).toInt
      val i = vstoreMatch.get.group(2).toInt
      return Seq((2 << 12) | (1 << 9) | (i << 6) | (j << 3) | 0)
    }

    // V<i> = V<j> + V<k>
    val vaddMatch = "^(?i)V(\\d+)\\s*=\\s*V(\\d+)\\s*\\+\\s*V(\\d+)".r.findFirstMatchIn(s)
    if (vaddMatch.isDefined) {
      val i = vaddMatch.get.group(1).toInt
      val j = vaddMatch.get.group(2).toInt
      val k = vaddMatch.get.group(3).toInt
      return Seq((3 << 12) | (2 << 9) | (i << 6) | (j << 3) | k)
    }

    try {
      Seq(parseNumericOrSymbol(s))
    } catch {
      case _: Exception =>
        throw new Exception(s"Unknown Cray-1 instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Cray-1 Hex File"
    for (valWord <- values) {
      out += f"${valWord.toLong & 0xFFFFFFFFFFFFFFFFL}%016X"
    }
    out.mkString("\n") + "\n"
  }
}
