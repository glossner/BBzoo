package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable
import java.util.regex.Pattern

class IbmstretchAssembler extends BaseAssembler with AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 64
  override val addressWidth: Int = 20
  override val hasLinkBit: Boolean = false
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

    val parts = s.split(" ", 2)
    val mnemonic = parts(0).toUpperCase

    if (mnemonic == "HLT") {
      return Seq(6 << 24)
    }

    val rest = if (parts.length > 1) parts(1).trim else ""

    if (Seq("LD", "ADD", "ST").contains(mnemonic)) {
      val op = mnemonic match {
        case "LD"  => 1
        case "ADD" => 2
        case "ST"  => 3
      }
      
      // Matches: ACC, addr(Xreg) or ACC, addr
      val pattern = Pattern.compile("(?i)^ACC,\\s*([A-Za-z0-9_]+)(?:\\((X[0-9]+)\\))?$")
      val matcher = pattern.matcher(rest)
      if (!matcher.matches()) {
        throw new Exception(s"Invalid IBM Stretch accumulator instruction format: $line")
      }
      
      val addrStr = matcher.group(1)
      val xregStr = matcher.group(2)
      
      val addr = parseNumericOrSymbol(addrStr)
      val xreg = if (xregStr != null) {
        xregStr.substring(1).toInt
      } else {
        0
      }
      
      return Seq((op << 24) | ((xreg & 0xF) << 20) | (addr & 0xFFFFF))
    }

    if (Seq("LDX", "ADDX").contains(mnemonic)) {
      val op = mnemonic match {
        case "LDX"  => 4
        case "ADDX" => 5
      }
      
      // Matches: Xreg, value
      val pattern = Pattern.compile("(?i)^(X[0-9]+),\\s*([A-Za-z0-9_]+)$")
      val matcher = pattern.matcher(rest)
      if (!matcher.matches()) {
        throw new Exception(s"Invalid IBM Stretch index instruction format: $line")
      }
      
      val xregStr = matcher.group(1)
      val valStr = matcher.group(2)
      
      val xreg = xregStr.substring(1).toInt
      val value = parseNumericOrSymbol(valStr)
      
      return Seq((op << 24) | ((xreg & 0xF) << 20) | (value & 0xFFFFF))
    }

    try {
      Seq(parseNumericOrSymbol(s))
    } catch {
      case _: Exception =>
        throw new Exception(s"Unknown IBM Stretch instruction: $s")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled IBM Stretch Hex File"
    for (valWord <- values) {
      val bigVal = if ((valWord & 0xFF000000) != 0) {
        val op = (valWord >> 24) & 0xFF
        val xreg = (valWord >> 20) & 0xF
        val addr = valWord & 0xFFFFF
        (BigInt(op) << 56) | (BigInt(xreg) << 52) | BigInt(addr)
      } else {
        BigInt(valWord.toLong & 0xFFFFFFFFL)
      }
      out += f"$bigVal%016X"
    }
    out.mkString("\n") + "\n"
  }
}
