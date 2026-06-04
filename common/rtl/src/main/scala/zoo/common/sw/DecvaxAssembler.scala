package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class DecvaxAssembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  commentPattern = "(;|//).*"
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 16
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 8
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = false

  case class Operand(mode: Int, reg: Int, imm: Option[String])

  def parseOperand(opStr: String): Operand = {
    val s = opStr.trim
    if (s.equalsIgnoreCase("PC")) {
      Operand(5, 15, None)
    } else if (s.equalsIgnoreCase("SP")) {
      Operand(5, 14, None)
    } else if (s.startsWith("#")) {
      Operand(8, 15, Some(s.substring(1)))
    } else if (s.startsWith("(") && s.endsWith(")+")) {
      val regName = s.substring(1, s.length - 2)
      val reg = if (regName.equalsIgnoreCase("SP")) 14 else regName.substring(1).toInt
      Operand(8, reg, None)
    } else if (s.startsWith("(") && s.endsWith(")")) {
      val regName = s.substring(1, s.length - 1)
      val reg = if (regName.equalsIgnoreCase("SP")) 14 else regName.substring(1).toInt
      Operand(6, reg, None)
    } else if (s.startsWith("R") || s.startsWith("r")) {
      val reg = s.substring(1).toInt
      Operand(5, reg, None)
    } else {
      throw new Exception(s"Invalid VAX operand: $s")
    }
  }

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return 0
    val parts = s.split(" ", 2)
    val mnemonic = parts(0).toUpperCase
    if (mnemonic == "HALT") return 1
    if (parts.length < 2) return 1

    val ops = parts(1).split(",")
    if (ops.length != 2) return 1

    var size = 1
    for (opStr <- ops) {
      val op = parseOperand(opStr)
      if (op.imm.isDefined) size += 1
    }
    size
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

    if (mnemonic == "HALT") return Seq(0)

    val ops = parts(1).split(",")
    val srcOp = parseOperand(ops(0))
    val dstOp = parseOperand(ops(1))

    val opVal = mnemonic match {
      case "MOVL"  => 0xD0
      case "ADDL2" => 0xC0
      case "SUBL2" => 0xC2
      case _       => throw new Exception(s"Unknown VAX instruction: $line")
    }

    val srcField = (srcOp.mode << 4) | srcOp.reg
    val dstField = (dstOp.mode << 4) | dstOp.reg
    val inst = (opVal << 24) | (srcField << 16) | (dstField << 8)

    val res = mutable.Buffer[Int]()
    res += inst

    if (srcOp.imm.isDefined) {
      res += parseNumericOrSymbol(srcOp.imm.get)
    }
    if (dstOp.imm.isDefined) {
      res += parseNumericOrSymbol(dstOp.imm.get)
    }

    res.toSeq
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled DEC VAX Hex File"
    for (valWord <- values) {
      out += f"${valWord & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
