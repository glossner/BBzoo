package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class MitDataflowAssembler extends BaseAssembler with StackArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val stackDepth: Int = 16
  override val opCodeWidth: Int = 8
  override val formats = Seq("Dataflow")
  override val hasMultiplyDivide: Boolean = false
  commentPattern = "(;|//).*"

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim
    if (s.isEmpty) return 0
    val parts = s.split("\\s+", 2)
    val op = parts(0).toUpperCase
    if (op == "ND_HALT" || op == "HALT") return 1
    if (op == "ND_ADD") return 1
    if (op == "ND_LOAD" || op == "ND_STORE") return 2
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

    if (op == "ND_HALT" || op == "HALT") {
      return Seq(0xFF << 24)
    }

    if (parts.length < 2) {
      throw new Exception(s"Missing operands for instruction: $line")
    }

    val ops = parts(1).split(",")

    if (op == "ND_LOAD") {
      if (ops.length != 3) {
        throw new Exception(s"ND_LOAD requires NodeID, Slot, MemoryAddress: $line")
      }
      val nodeId = ops(0).trim.toInt
      val slot = ops(1).trim.toInt
      val addr = parseNumericOrSymbol(ops(2))
      val inst = (0x40 << 24) | (nodeId << 16) | (slot << 8)
      return Seq(inst, addr)
    }

    if (op == "ND_STORE") {
      if (ops.length != 2) {
        throw new Exception(s"ND_STORE requires Slot, MemoryAddress: $line")
      }
      val slot = ops(0).trim.toInt
      val addr = parseNumericOrSymbol(ops(1))
      val inst = (0x42 << 24) | (slot << 8)
      return Seq(inst, addr)
    }

    if (op == "ND_ADD") {
      if (ops.length != 1) {
        throw new Exception(s"ND_ADD requires TargetNodeID: $line")
      }
      val nodeId = ops(0).trim.toInt
      val inst = (0x41 << 24) | (nodeId << 16)
      return Seq(inst)
    }

    throw new Exception(s"Unknown MIT Dataflow instruction: $line")
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled MIT Dataflow Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
