package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class MultiflowAssembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 8
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 4
  override val formats = Seq("Single")
  override val hasMultiplyDivide: Boolean = false
  commentPattern = "(#|//).*"

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim
    if (s.isEmpty) return 0
    // 2 words of 32-bit = 64-bit VLIW bundle
    2
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
    val s = regStr.trim.toUpperCase
    if (s.startsWith("R")) {
      s.substring(1).toInt
    } else {
      throw new Exception(s"Invalid register: $regStr")
    }
  }

  override def assembleInstruction(line: String): Seq[Int] = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return Seq()

    val upper = s.toUpperCase
    if (upper == "HLT" || upper == "HALT") {
      return Seq(0, 2 << 20)
    }
    if (upper.startsWith("JMP ")) {
      val target = parseNumericOrSymbol(s.substring(4))
      return Seq(0, (1 << 20) | (target & 0xFFFF))
    }
    if (upper.startsWith("LD_CU ")) {
      val parts = s.substring(6).split(",")
      val rd = parseReg(parts(0))
      val target = parseNumericOrSymbol(parts(1))
      return Seq(0, (3 << 20) | (rd << 16) | (target & 0xFFFF))
    }

    // Otherwise, parse VLIW instruction (ALU ; LSU ; CTRL)
    val slots = s.split(";")
    if (slots.length != 3) {
      throw new Exception(s"Multiflow instruction must have exactly 3 slots separated by semicolons: $line")
    }

    var aluField = 0
    var lsuField = 0
    var ctrlField = 0

    // 1. ALU Slot (ADD, SUB, NOP)
    val aluStr = slots(0).trim.toUpperCase
    if (aluStr != "NOP") {
      val parts = aluStr.split(" ", 2)
      val op = parts(0)
      val operands = parts(1).split(",")
      val rd = parseReg(operands(0))
      val rs1 = parseReg(operands(1))
      val rs2 = parseReg(operands(2))
      val opCode = op match {
        case "ADD" => 1
        case "SUB" => 2
        case _     => throw new Exception(s"Unknown ALU op: $op")
      }
      aluField = (opCode << 9) | (rd << 6) | (rs1 << 3) | rs2
    }

    // 2. LSU Slot (LD, ST, LD_CU, NOP)
    val lsuStr = slots(1).trim.toUpperCase
    if (lsuStr != "NOP") {
      val parts = lsuStr.split(" ", 2)
      val op = parts(0)
      val operands = parts(1).split(",")
      val reg = parseReg(operands(0))
      val base = parseReg(operands(1))
      val opCode = op match {
        case "LD"    => 1
        case "ST"    => 2
        case "LD_CU" => 3
        case _       => throw new Exception(s"Unknown LSU op: $op")
      }
      lsuField = (opCode << 6) | (reg << 3) | base
    }

    // 3. CTRL Slot (HLT, JMP, NOP)
    val ctrlStr = slots(2).trim.toUpperCase
    if (ctrlStr != "NOP") {
      val parts = ctrlStr.split(" ", 2)
      val op = parts(0)
      val opCode = op match {
        case "JMP" => 1
        case "HLT" => 2
        case _     => throw new Exception(s"Unknown CTRL op: $op")
      }
      val target = if (op == "JMP") parseNumericOrSymbol(parts(1)) else 0
      ctrlField = (opCode << 20) | (target & 0xFFFF)
    }

    val w0 = (aluField << 16) | (lsuField & 0xFFFF)
    val w1 = ctrlField
    Seq(w0, w1)
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Multiflow Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
