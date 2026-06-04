package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Upd7720Assembler extends BaseAssembler with AccumulatorArchitecture with VariableLengthFormat with FixedPointOperations {
  commentPattern = "(;|//).*"
  override val wordWidth: Int = 16
  override val addressWidth: Int = 16
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 8
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = true

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return 0
    val parts = s.split(" ", 2)
    val mnemonic = parts(0).toUpperCase
    if (mnemonic == "HALT") return 1
    if (mnemonic == "ADD") return 1
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

    if (mnemonic == "HALT") return Seq(0x00 << 8)
    if (mnemonic == "ADD") return Seq(0x12 << 8)

    val ops = parts(1).split(",")

    mnemonic match {
      case "LD" =>
        if (ops.length != 2) throw new Exception(s"LD requires 2 operands: $s")
        val reg = ops(0).trim.toUpperCase
        val addr = parseNumericOrSymbol(ops(1))
        if (reg == "A") Seq(0x10 << 8, addr)
        else if (reg == "B") Seq(0x11 << 8, addr)
        else throw new Exception(s"Invalid register for LD in uPD7720: $reg")

      case "ST" =>
        if (ops.length != 2) throw new Exception(s"ST requires 2 operands: $s")
        val reg = ops(0).trim.toUpperCase
        val addr = parseNumericOrSymbol(ops(1))
        if (reg == "A") Seq(0x13 << 8, addr)
        else throw new Exception(s"Invalid register for ST in uPD7720: $reg")

      case _ =>
        throw new Exception(s"Unknown uPD7720 instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled uPD7720 Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFF}%04X"
    }
    out.mkString("\n") + "\n"
  }
}
