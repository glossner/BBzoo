package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Pdp8Assembler extends BaseAssembler with AccumulatorArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth = 12
  override val addressWidth = 12
  override val hasLinkBit = true
  override val opCodeWidth = 3
  override val instructionWidth = 12
  override val hasMultiplyDivide = false

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
    val parts = line.split("\\s+")
    if (parts.isEmpty) return Seq(0)

    val mnemonic = parts(0).toUpperCase

    // Operate / microinstructions
    if (mnemonic == "CLA" && parts.length > 1 && parts(1).toUpperCase == "CLL") {
      return Seq(0xE00) // Octal 7000
    } else if (mnemonic == "CLA" && parts.length == 1) {
      return Seq(0xE80) // Octal 7200
    } else if (mnemonic == "HLT") {
      return Seq(0xF02) // Octal 7402
    }

    if (line.toUpperCase.contains("CLA CLL")) {
      return Seq(0xE00)
    }

    // Memory reference instructions
    val mriOpcodes = Map(
      "AND" -> 0,
      "TAD" -> 1,
      "ISZ" -> 2,
      "DCA" -> 3,
      "JMS" -> 4,
      "JMP" -> 5
    )

    if (mriOpcodes.contains(mnemonic)) {
      val opcode = mriOpcodes(mnemonic)
      var indirect = 0
      var addrStr = ""

      if (parts.length == 3 && parts(1).toUpperCase == "I") {
        indirect = 1
        addrStr = parts(2)
      } else if (parts.length == 2) {
        addrStr = parts(1)
      } else {
        throw new Exception(s"Invalid MRI instruction format: $line")
      }

      val addrVal = parseNumericOrSymbol(addrStr)
      
      // Z page bit logic
      var pageBit = 0
      var offset = 0
      if ((addrVal & 0xF80) == 0) {
        pageBit = 0
        offset = addrVal & 0x7F
      } else if ((addrVal & 0xF80) == (pc & 0xF80)) {
        pageBit = 1
        offset = addrVal & 0x7F
      } else {
        throw new Exception(s"Address $addrVal is not reachable from PC $pc (must be Page 0 or current page)")
      }

      val instWord = (opcode << 9) | (indirect << 8) | (pageBit << 7) | offset
      return Seq(instWord)
    }

    // Check if it is a raw number (DATA)
    try {
      Seq(parseNumericOrSymbol(parts(0)))
    } catch {
      case _: Exception =>
        throw new Exception(s"Unknown instruction/operand: $line")
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled PDP-8 Hex File"
    for (valWord <- values) {
      out += f"${valWord & 0xFFF}%03X"
    }
    out.mkString("\n") + "\n"
  }
}
