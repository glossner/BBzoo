package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class BabbageAssembler extends BaseAssembler with StackArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 64
  override val addressWidth: Int = 16
  override val stackDepth: Int = 16
  override val opCodeWidth: Int = 8
  override val instructionWidth: Int = 64
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

    if (mnemonic == "HLT") {
      return Seq(5 << 16)
    }

    if (Seq("L", "S", "ADD", "SUB").contains(mnemonic)) {
      val op = mnemonic match {
        case "L"   => 1
        case "S"   => 2
        case "ADD" => 3
        case "SUB" => 4
      }
      val addr = parseNumericOrSymbol(parts(1))
      return Seq((op << 16) | (addr & 0xFFFF))
    }

    try {
      Seq(parseNumericOrSymbol(s))
    } catch {
      case _: Exception =>
        throw new Exception(s"Unknown Babbage instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Babbage Hex File"
    for (valWord <- values) {
      val longVal = if ((valWord & 0xFFFF0000) != 0) {
        val opcode = ((valWord >> 16) & 0xFFFF).toLong
        val addr = (valWord & 0xFFFF).toLong
        (opcode << 32) | addr
      } else {
        valWord.toLong & 0xFFFFFFFFFFFFFFFFL
      }
      out += f"$longVal%016X"
    }
    out.mkString("\n") + "\n"
  }
}
