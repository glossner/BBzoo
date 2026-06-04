package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class CrusoeAssembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 8
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 4
  override val formats = Seq("Single")
  override val hasMultiplyDivide: Boolean = false

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim
    if (s.isEmpty) return 0
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
    val s = regStr.trim.toUpperCase
    if (s.startsWith("R")) {
      s.substring(1).toInt
    } else if (s.startsWith("EAX")) {
      0
    } else if (s.startsWith("EBX")) {
      1
    } else if (s.startsWith("ECX")) {
      2
    } else if (s.startsWith("EDX")) {
      3
    } else {
      throw new Exception(s"Invalid register: $regStr")
    }
  }

  override def assembleInstruction(line: String): Seq[Int] = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return Seq()

    val parts = s.split(" ", 2)
    val op = parts(0).toUpperCase
    
    if (op == "HLT") {
      return Seq(7 << 28)
    }

    if (op == "JMP") {
      val target = parseNumericOrSymbol(parts(1))
      return Seq((6 << 28) | (target & 0xFFFF))
    }

    if (op == "LD_CU") {
      val operands = parts(1).split(",")
      val rd = parseReg(operands(0))
      val target = parseNumericOrSymbol(operands(1))
      return Seq((5 << 28) | (rd << 24) | (target & 0xFFFF))
    }

    val operands = parts(1).split(",")
    if (op == "MOV") {
      val dest = operands(0).trim
      val src = operands(1).trim
      
      if (dest.startsWith("[") && dest.endsWith("]")) {
        // MOV [rd], rs -> Store
        val rdStr = dest.stripPrefix("[").stripSuffix("]")
        val rd = parseReg(rdStr)
        val rs = parseReg(src)
        Seq((2 << 28) | (rd << 24) | (rs << 20))
      } else if (src.startsWith("[") && src.endsWith("]")) {
        // MOV rd, [rs] -> Load
        val rd = parseReg(dest)
        val rsStr = src.stripPrefix("[").stripSuffix("]")
        val rs = parseReg(rsStr)
        Seq((1 << 28) | (rd << 24) | (rs << 20))
      } else {
        // Simple register-to-register move as addition with R0 (assumed 0)
        val rd = parseReg(dest)
        val rs = parseReg(src)
        Seq((3 << 28) | (rd << 24) | (rs << 20))
      }
    } else {
      val rd = parseReg(operands(0))
      val rs = parseReg(operands(1))
      val opCode = op match {
        case "ADD" => 3
        case "SUB" => 4
        case _     => throw new Exception(s"Unknown Crusoe op: $op")
      }
      Seq((opCode << 28) | (rd << 24) | (rs << 20))
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Transmeta Crusoe Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
