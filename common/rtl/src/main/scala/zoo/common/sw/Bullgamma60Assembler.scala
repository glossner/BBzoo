package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Bullgamma60Assembler extends BaseAssembler with AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 24
  override val addressWidth: Int = 16
  override val hasLinkBit: Boolean = false
  override val opCodeWidth: Int = 6
  override val instructionWidth: Int = 24
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
      return Seq(6 << 18)
    }
    if (mnemonic == "JOIN") {
      return Seq(5 << 18)
    }
    if (mnemonic == "FORK") {
      val addr = parseNumericOrSymbol(parts(1))
      return Seq((4 << 18) | (addr & 0xFFFF))
    }

    if (Seq("LD", "ADD", "ST").contains(mnemonic)) {
      val op = mnemonic match {
        case "LD"  => 1
        case "ADD" => 2
        case "ST"  => 3
      }
      val regStr = parts(1).replace(",", "").trim.toUpperCase
      val addrStr = parts(2).trim
      
      val regVal = regStr match {
        case "ACC1" => 1
        case "ACC0" => 0
        case _ =>
          try {
            parseNumericOrSymbol(regStr)
          } catch {
            case _: Exception => 0
          }
      }
      val addr = parseNumericOrSymbol(addrStr)
      return Seq((op << 18) | ((regVal & 3) << 16) | (addr & 0xFFFF))
    }

    try {
      Seq(parseNumericOrSymbol(s))
    } catch {
      case _: Exception =>
        throw new Exception(s"Unknown Bull Gamma 60 instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Bull Gamma 60 Hex File"
    for (valWord <- values) {
      out += f"${valWord & 0xFFFFFF}%06X"
    }
    out.mkString("\n") + "\n"
  }
}
