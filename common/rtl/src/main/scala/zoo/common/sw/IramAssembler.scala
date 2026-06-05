package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class IramAssembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 32
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 6
  override val formats = Seq("R", "I")
  override val hasMultiplyDivide: Boolean = false
  commentPattern = "(;|//).*"

  def parseScalarReg(regStr: String): Int = {
    val s = regStr.trim
    if (s.toUpperCase.startsWith("R")) {
      s.substring(1).toInt
    } else {
      throw new Exception(s"Invalid IRAM scalar register: $regStr")
    }
  }

  def parseVectorReg(regStr: String): Int = {
    val s = regStr.trim
    if (s.toUpperCase.startsWith("V")) {
      s.substring(1).toInt
    } else {
      throw new Exception(s"Invalid IRAM vector register: $regStr")
    }
  }

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim
    if (s.isEmpty) return 0
    val parts = s.split("\\s+", 2)
    val op = parts(0).toUpperCase
    if (op == "LW" || op == "SW") return 2
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

  override def assembleInstruction(line: String): Seq[Int] = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return Seq()

    val parts = s.split(" ", 2)
    val op = parts(0).toUpperCase

    if (op == "HLT" || op == "HALT") {
      return Seq(0x3F << 26)
    }

    if (parts.length < 2) {
      throw new Exception(s"Missing operands for instruction: $line")
    }

    val ops = parts(1).split(",")

    if (op == "VLD") {
      if (ops.length != 2) throw new Exception(s"VLD instruction requires 2 operands: $line")
      val vd = parseVectorReg(ops(0))
      val rs = parseScalarReg(ops(1))
      Seq((0x01 << 26) | (vd << 21) | (rs << 16))
    } else if (op == "VST") {
      if (ops.length != 2) throw new Exception(s"VST instruction requires 2 operands: $line")
      val vs = parseVectorReg(ops(0))
      val rd = parseScalarReg(ops(1))
      Seq((0x02 << 26) | (vs << 21) | (rd << 16))
    } else if (op == "VADD.W") {
      if (ops.length != 3) throw new Exception(s"VADD.W instruction requires 3 operands: $line")
      val vd = parseVectorReg(ops(0))
      val va = parseVectorReg(ops(1))
      val vb = parseVectorReg(ops(2))
      Seq((0x03 << 26) | (vd << 21) | (va << 16) | (vb << 11))
    } else if (op == "LW") {
      if (ops.length != 2) throw new Exception(s"LW instruction requires 2 operands: $line")
      val rd = parseScalarReg(ops(0))
      val addr = parseNumericOrSymbol(ops(1))
      Seq((0x04 << 26) | (rd << 21), addr)
    } else if (op == "SW") {
      if (ops.length != 2) throw new Exception(s"SW instruction requires 2 operands: $line")
      val rs = parseScalarReg(ops(0))
      val addr = parseNumericOrSymbol(ops(1))
      Seq((0x05 << 26) | (rs << 21), addr)
    } else {
      throw new Exception(s"Unknown IRAM instruction: $line")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled IRAM Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
