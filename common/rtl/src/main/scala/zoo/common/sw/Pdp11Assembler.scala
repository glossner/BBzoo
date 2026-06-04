package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Pdp11Assembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  commentPattern = "(;|//).*"
  override val wordWidth: Int = 16
  override val addressWidth: Int = 16
  override val numGPRs: Int = 8
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 4
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = false

  case class Operand(mode: Int, reg: Int, imm: Option[String])

  def parseOperand(opStr: String): Operand = {
    val s = opStr.trim
    if (s.equalsIgnoreCase("PC")) {
      Operand(0, 7, None)
    } else if (s.startsWith("#")) {
      Operand(2, 7, Some(s.substring(1)))
    } else if (s.startsWith("(") && s.endsWith(")+")) {
      val regName = s.substring(1, s.length - 2)
      val reg = regName.substring(1).toInt
      Operand(2, reg, None)
    } else if (s.startsWith("(") && s.endsWith(")")) {
      val regName = s.substring(1, s.length - 1)
      val reg = regName.substring(1).toInt
      Operand(1, reg, None)
    } else if (s.startsWith("R") || s.startsWith("r")) {
      val reg = s.substring(1).toInt
      Operand(0, reg, None)
    } else {
      throw new Exception(s"Invalid PDP-11 operand: $s")
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
      case "MOV" => 1
      case "ADD" => 2
      case "SUB" => 3
      case _     => throw new Exception(s"Unknown PDP-11 instruction: $line")
    }

    val srcField = (srcOp.mode << 3) | srcOp.reg
    val dstField = (dstOp.mode << 3) | dstOp.reg
    val inst = (opVal << 12) | (srcField << 6) | dstField

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
    out += "# Compiled DEC PDP-11 Hex File"
    for (valWord <- values) {
      out += f"${valWord & 0xFFFF}%04X"
    }
    out.mkString("\n") + "\n"
  }
}
