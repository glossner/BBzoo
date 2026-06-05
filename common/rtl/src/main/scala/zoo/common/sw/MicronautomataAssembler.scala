package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class MicronautomataAssembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 2
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 6
  override val formats = Seq("R", "I")
  override val hasMultiplyDivide: Boolean = false
  commentPattern = "(;|//).*"

  def parseStateReg(regStr: String): Int = {
    val s = regStr.trim
    if (s.toUpperCase.startsWith("R")) {
      val v = s.substring(1).toInt
      if (v == 0 || v == 1) return v
    }
    throw new Exception(s"Invalid Micron Automata state register (must be R0 or R1): $regStr")
  }

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim
    if (s.isEmpty) return 0
    val parts = s.split("\\s+", 2)
    val op = parts(0).toUpperCase
    if (op == "STATE_IN" || op == "STATE_OUT") return 2
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

    if (op == "STATE_IN") {
      if (ops.length != 2) throw new Exception(s"STATE_IN requires 2 operands: $line")
      val r_in = parseStateReg(ops(0))
      val addr = parseNumericOrSymbol(ops(1))
      Seq((0x01 << 26) | (r_in << 21), addr)
    } else if (op == "STATE_OUT") {
      if (ops.length != 2) throw new Exception(s"STATE_OUT requires 2 operands: $line")
      val r_out = parseStateReg(ops(0))
      val addr = parseNumericOrSymbol(ops(1))
      Seq((0x03 << 26) | (r_out << 21), addr)
    } else if (op == "STATE_ADD") {
      if (ops.length != 3) throw new Exception(s"STATE_ADD requires 3 operands: $line")
      val r_out = parseStateReg(ops(0))
      val r_in1 = parseStateReg(ops(1))
      val r_in2 = parseStateReg(ops(2))
      Seq((0x02 << 26) | (r_out << 21) | (r_in1 << 16) | (r_in2 << 11))
    } else {
      throw new Exception(s"Unknown Micron Automata instruction: $line")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Micron Automata Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
