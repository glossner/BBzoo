package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class IbmmfastAssembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 16
  override val addressWidth: Int = 16
  override val numGPRs: Int = 8
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 4
  override val formats = Seq("Single", "Double")
  override val hasMultiplyDivide: Boolean = true
  commentPattern = "(#|//).*"

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return 0
    // Every IBM MFAST VLIW instruction is 32-bit (2 words of 16-bit)
    2
  }

  def parseNumericOrSymbol(valStr: String): Int = {
    val s = valStr.trim
    if (symbols.contains(s)) {
      symbols(s)
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

    if (s.toUpperCase == "HALT" || s.toUpperCase == "HLT") {
      // Control op = 10 (HLT) -> 0x8000_0000
      return Seq(0x8000, 0x0000)
    }

    if (s.toUpperCase.startsWith("JMP")) {
      val target = parseNumericOrSymbol(s.substring(3))
      // Control op = 01 (JMP) -> 0x4000_0000 | target
      return Seq(0x4000, target & 0xFFFF)
    }

    if (s.toUpperCase.startsWith("LD_CU")) {
      val parts = s.split(" ", 2)(1).split(",")
      val rd = parseReg(parts(0))
      val addr = parseNumericOrSymbol(parts(1))
      // Control op = 11 (LD_CU) -> 0xC000_0000 | (rd << 28) | addr
      return Seq(0xC000 | (rd << 8), addr & 0xFFFF)
    }

    // Otherwise, parse VLIW instruction (ALU ; MAU ; LSU)
    val slots = s.split(";")
    if (slots.length != 3) {
      throw new Exception(s"VLIW instruction must have exactly 3 slots separated by semicolons: $line")
    }

    var aluField = 0
    var mauField = 0
    var lsuField = 0

    // 1. ALU Slot
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
        case "AND" => 3
        case _     => throw new Exception(s"Unknown ALU op: $op")
      }
      aluField = (opCode << 8) | (rd << 5) | (rs1 << 2) | (rs2 & 0x3)
    }

    // 2. MAU Slot
    val mauStr = slots(1).trim.toUpperCase
    if (mauStr != "NOP") {
      val parts = mauStr.split(" ", 2)
      val op = parts(0)
      val operands = parts(1).split(",")
      val rd = parseReg(operands(0))
      val rs1 = parseReg(operands(1))
      val rs2 = parseReg(operands(2))
      val opCode = op match {
        case "MUL"  => 1
        case "MAC"  => 2
        case "MSUB" => 3
        case _      => throw new Exception(s"Unknown MAU op: $op")
      }
      mauField = (opCode << 8) | (rd << 5) | (rs1 << 2) | (rs2 & 0x3)
    }

    // 3. LSU Slot
    val lsuStr = slots(2).trim.toUpperCase
    if (lsuStr != "NOP") {
      val parts = lsuStr.split("\\s+", 2)
      val op = parts(0)
      val operands = parts(1).split(",")
      val reg = parseReg(operands(0))
      val base = parseReg(operands(1))
      val opCode = op match {
        case "LD" => 1
        case "ST" => 2
        case _    => throw new Exception(s"Unknown LSU op: $op")
      }
      lsuField = (opCode << 8) | (reg << 5) | (base & 0x1F)
    }

    // Control op = 00 -> VLIW
    // Pack fields:
    // Word 0: [15, 14] = 00, [13, 10] = ALU Op/Rd/Rs1_high?
    // Wait, let's keep it simple:
    // Word 0: ALU Field (bits 29-20) -> goes to bits [13, 4] of Word 0?
    // Let's just pack:
    // Word 0 = (aluField << 4) | (mauField >> 6)
    // Word 1 = (mauField << 10) | lsuField
    // Wait, let's look at the bit mapping:
    // Word 0 is bits [31, 16] of the 32-bit instruction.
    // Word 1 is bits [15, 0] of the 32-bit instruction.
    // If we map:
    // Bits [31, 30]: Control (00)
    // Bits [29, 20]: ALU Field (10 bits) -> Word 0 bits [13, 4]
    // Bits [19, 10]: MAU Field (10 bits) -> Word 0 bits [3, 0] and Word 1 bits [15, 10]
    // Bits [9, 0]: LSU Field (10 bits) -> Word 1 bits [9, 0]
    // This is a direct bit-packing!
    val inst32 = (aluField.toLong << 20) | (mauField.toLong << 10) | lsuField.toLong
    val w0 = ((inst32 >> 16) & 0xFFFF).toInt
    val w1 = (inst32 & 0xFFFF).toInt
    Seq(w0, w1)
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled IBM MFAST Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFF}%04X"
    }
    out.mkString("\n") + "\n"
  }
}
