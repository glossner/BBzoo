package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class TtaAssembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 8
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 6
  override val formats = Seq("Move")
  override val hasMultiplyDivide: Boolean = false
  commentPattern = "(;|//).*"

  val sockets = mutable.Map[String, Int](
    "ADD_IN1" -> 8,
    "ADD_IN2" -> 9,
    "ADD_OUT" -> 10,
    "LSU_ADDR" -> 11,
    "LSU_RDATA" -> 12,
    "LSU_WDATA" -> 13
  )
  for (i <- 0 until 8) {
    sockets(s"R$i") = i
  }

  def parseSocket(sStr: String): Int = {
    val s = sStr.trim.toUpperCase
    if (sockets.contains(s)) {
      sockets(s)
    } else {
      throw new Exception(s"Invalid TTA socket: $sStr")
    }
  }

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim
    if (s.isEmpty) return 0
    val parts = s.split("\\s+", 2)
    val op = parts(0).toUpperCase
    if (op == "HLT" || op == "HALT") return 1
    if (op == "MOVE_IMM") return 2
    if (op == "MOVE") return 1
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

    if (op == "MOVE") {
      if (ops.length != 2) {
        throw new Exception(s"MOVE instruction requires exactly 2 operands: $line")
      }
      val src = parseSocket(ops(0))
      val dest = parseSocket(ops(1))
      val inst = (0x00 << 26) | (src << 20) | (dest << 14)
      return Seq(inst)
    }

    if (op == "MOVE_IMM") {
      if (ops.length != 2) {
        throw new Exception(s"MOVE_IMM instruction requires exactly 2 operands: $line")
      }
      val imm = parseNumericOrSymbol(ops(0))
      val dest = parseSocket(ops(1))
      val inst = (0x01 << 26) | (dest << 14)
      return Seq(inst, imm)
    }

    throw new Exception(s"Unknown TTA instruction: $line")
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled TTA Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
