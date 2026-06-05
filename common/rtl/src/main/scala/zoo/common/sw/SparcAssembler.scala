package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class SparcAssembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 32
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 6
  override val formats = Seq("R", "I")
  override val hasMultiplyDivide: Boolean = false
  commentPattern = "(;|//).*"

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim
    if (s.isEmpty) return 0
    val parts = s.split("\\s+", 2)
    val op = parts(0).toUpperCase
    if (op == "HLT" || op == "HALT") return 1
    if (op == "ADD") return 1
    if (op == "LD" || op == "ST") return 2
    1
  }

  def parseNumericOrSymbol(valStr: String): Int = {
    val s = valStr.trim
    val key = symbols.keys.find(_.equalsIgnoreCase(s))
    if (key.isDefined) {
      symbols(key.get)
    } else {
      parseNumeric(s)
    }
  }

  def parseReg(regStr: String): Int = {
    var s = regStr.trim.toLowerCase
    if (s.startsWith("%")) {
      s = s.substring(1)
    }
    if (s.startsWith("g")) {
      val v = s.substring(1).toInt
      if (v >= 0 && v <= 7) return v
    } else if (s.startsWith("o")) {
      val v = s.substring(1).toInt
      if (v >= 0 && v <= 7) return 8 + v
    } else if (s.startsWith("l")) {
      val v = s.substring(1).toInt
      if (v >= 0 && v <= 7) return 16 + v
    } else if (s.startsWith("i")) {
      val v = s.substring(1).toInt
      if (v >= 0 && v <= 7) return 24 + v
    } else if (s.startsWith("r")) {
      val v = s.substring(1).toInt
      if (v >= 0 && v <= 31) return v
    }
    throw new Exception(s"Invalid SPARC register: $regStr")
  }

  override def assembleInstruction(line: String): Seq[Int] = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return Seq()

    val parts = s.split(" ", 2)
    val op = parts(0).toUpperCase

    if (op == "HLT" || op == "HALT") {
      return Seq(0x3F << 26)
    }

    val operands = parts(1).split(",")

    if (op == "LD") {
      val addrStr = operands(0).trim.stripPrefix("[").stripSuffix("]")
      val addr = parseNumericOrSymbol(addrStr)
      val rd = parseReg(operands(1))
      val inst = (0x02 << 26) | (rd << 16)
      return Seq(inst, addr)
    }

    if (op == "ST") {
      val rs = parseReg(operands(0))
      val addrStr = operands(1).trim.stripPrefix("[").stripSuffix("]")
      val addr = parseNumericOrSymbol(addrStr)
      val inst = (0x03 << 26) | (rs << 16)
      return Seq(inst, addr)
    }

    if (op == "ADD") {
      val rs1 = parseReg(operands(0))
      val rs2 = parseReg(operands(1))
      val rd = parseReg(operands(2))
      val inst = (0x01 << 26) | (rd << 21) | (rs1 << 16) | (rs2 << 11)
      return Seq(inst)
    }

    throw new Exception(s"Unknown SPARC instruction: $line")
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled SPARC Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
