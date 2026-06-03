package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class M68kAssembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth = 32
  override val addressWidth = 32
  override val numGPRs = 16
  override val numFPRs = 0
  override val opCodeWidth = 4
  override val formats = Seq("1-Word", "2-Word", "3-Word")
  override val hasMultiplyDivide = true

  commentPattern = "(;|//).*" // Keep '#' for immediate values!

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim
    if (s.isEmpty) return 0
    if ("^(?i)MOVEA?\\.L\\s+#".r.findFirstIn(s).isDefined) {
      return 3
    }
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

    // 1. MOVEA.L #imm, An
    val moveaMatch = "^(?i)MOVEA\\.L\\s+#([^,\\s]+)\\s*,\\s*A(\\d+)".r.findFirstMatchIn(s)
    if (moveaMatch.isDefined) {
      val symbolOrNum = moveaMatch.get.group(1)
      var immVal = parseNumericOrSymbol(symbolOrNum)
      if (symbols.contains(symbolOrNum)) {
        immVal = immVal * 2 // Label is word address, convert to byte address
      }
      val n = moveaMatch.get.group(2).toInt
      val opcode = 0x207C | (n << 9)
      return Seq(opcode, (immVal >> 16) & 0xFFFF, immVal & 0xFFFF)
    }

    // 2. MOVE.L #imm, Dn
    val moveliMatch = "^(?i)MOVE\\.L\\s+#([^,\\s]+)\\s*,\\s*D(\\d+)".r.findFirstMatchIn(s)
    if (moveliMatch.isDefined) {
      val symbolOrNum = moveliMatch.get.group(1)
      var immVal = parseNumericOrSymbol(symbolOrNum)
      if (symbols.contains(symbolOrNum)) {
        immVal = immVal * 2
      }
      val n = moveliMatch.get.group(2).toInt
      val opcode = 0x203C | (n << 9)
      return Seq(opcode, (immVal >> 16) & 0xFFFF, immVal & 0xFFFF)
    }

    // 3. MOVE.L (Am), Dn
    val moveldMatch = "^(?i)MOVE\\.L\\s*\\(A(\\d+)\\)\\s*,\\s*D(\\d+)".r.findFirstMatchIn(s)
    if (moveldMatch.isDefined) {
      val m = moveldMatch.get.group(1).toInt
      val n = moveldMatch.get.group(2).toInt
      return Seq(0x2010 | (n << 9) | m)
    }

    // 4. MOVE.L Dn, (Am)
    val movestMatch = "^(?i)MOVE\\.L\\s*D(\\d+)\\s*,\\s*\\(A(\\d+)\\)".r.findFirstMatchIn(s)
    if (movestMatch.isDefined) {
      val n = movestMatch.get.group(1).toInt
      val m = movestMatch.get.group(2).toInt
      return Seq(0x2080 | (m << 9) | n)
    }

    // 5. MOVE.L Dm, Dn
    val moverrMatch = "^(?i)MOVE\\.L\\s*D(\\d+)\\s*,\\s*D(\\d+)".r.findFirstMatchIn(s)
    if (moverrMatch.isDefined) {
      val m = moverrMatch.get.group(1).toInt
      val n = moverrMatch.get.group(2).toInt
      return Seq(0x2000 | (n << 9) | m)
    }

    // 6. ADD.L Dm, Dn
    val addMatch = "^(?i)ADD\\.L\\s*D(\\d+)\\s*,\\s*D(\\d+)".r.findFirstMatchIn(s)
    if (addMatch.isDefined) {
      val m = addMatch.get.group(1).toInt
      val n = addMatch.get.group(2).toInt
      return Seq(0xD080 | (n << 9) | m)
    }

    // 7. SUB.L Dm, Dn
    val subMatch = "^(?i)SUB\\.L\\s*D(\\d+)\\s*,\\s*D(\\d+)".r.findFirstMatchIn(s)
    if (subMatch.isDefined) {
      val m = subMatch.get.group(1).toInt
      val n = subMatch.get.group(2).toInt
      return Seq(0x9080 | (n << 9) | m)
    }

    // 8. BRA target
    val braMatch = "^(?i)BRA\\s+(\\S+)".r.findFirstMatchIn(s)
    if (braMatch.isDefined) {
      val target = braMatch.get.group(1)
      val targetPc = parseNumericOrSymbol(target)
      val offset = (targetPc - (pc + 1)) * 2
      return Seq(0x6000 | (offset & 0xFF))
    }

    try {
      Seq(parseNumericOrSymbol(s))
    } catch {
      case _: Exception =>
        throw new Exception(s"Unknown Motorola 68000 instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Motorola 68000 Hex File"
    for (valWord <- values) {
      out += f"${valWord & 0xFFFF}%04X"
    }
    out.mkString("\n") + "\n"
  }
}
