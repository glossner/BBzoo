package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Cdc6600Assembler extends BaseAssembler with GeneralRegisterArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 60
  override val addressWidth: Int = 18
  override val numGPRs: Int = 24
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 6
  override val instructionWidth: Int = 60
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

    if (s.equalsIgnoreCase("HLT")) {
      return Seq(0)
    }

    // Ai = Bj + K
    val addrMatch = "^A([0-7])\\s*=\\s*B([0-7])\\s*\\+\\s*(\\S+)".r.findFirstMatchIn(s)
    if (addrMatch.isDefined) {
      val i = addrMatch.get.group(1).toInt
      val j = addrMatch.get.group(2).toInt
      val K = parseNumericOrSymbol(addrMatch.get.group(3))
      // Encode in 32-bit: opcode = 1
      val valWord = (1 << 26) | (i << 23) | (j << 20) | (K & 0x1FFFF)
      return Seq(valWord)
    }

    // Xi = Xj + Xk
    val addMatch = "^X([0-7])\\s*=\\s*X([0-7])\\s*\\+\\s*X([0-7])".r.findFirstMatchIn(s)
    if (addMatch.isDefined) {
      val i = addMatch.get.group(1).toInt
      val j = addMatch.get.group(2).toInt
      val k = addMatch.get.group(3).toInt
      // Encode in 32-bit: opcode = 2
      val valWord = (2 << 26) | (i << 23) | (j << 20) | (k << 17)
      return Seq(valWord)
    }

    // Xi = Xj - Xk
    val subMatch = "^X([0-7])\\s*=\\s*X([0-7])\\s*-\\s*X([0-7])".r.findFirstMatchIn(s)
    if (subMatch.isDefined) {
      val i = subMatch.get.group(1).toInt
      val j = subMatch.get.group(2).toInt
      val k = subMatch.get.group(3).toInt
      // Encode in 32-bit: opcode = 3
      val valWord = (3 << 26) | (i << 23) | (j << 20) | (k << 17)
      return Seq(valWord)
    }

    try {
      Seq(parseNumericOrSymbol(s))
    } catch {
      case _: Exception =>
        throw new Exception(s"Unknown CDC 6600 instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled CDC 6600 Hex File"
    for (valWord <- values) {
      val longVal = if ((valWord & 0xFC000000) != 0) {
        val opcode = ((valWord >> 26) & 0x3F).toLong
        val i = ((valWord >> 23) & 0x7).toLong
        val j = ((valWord >> 20) & 0x7).toLong
        val k = ((valWord >> 17) & 0x7).toLong
        val K = (valWord & 0x1FFFF).toLong
        (opcode << 54) | (i << 51) | (j << 48) | (k << 45) | (K << 27)
      } else {
        valWord.toLong & 0xFFFFFFFFFFFFFFFL
      }
      out += f"$longVal%015X"
    }
    out.mkString("\n") + "\n"
  }
}
