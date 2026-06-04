package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class Tms320c6kAssembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 8
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 4
  override val formats = Seq("Single")
  override val hasMultiplyDivide: Boolean = false

  // Map to track which instruction addresses have the p-bit set
  private val parallelPcs = mutable.Set[Int]()

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
    } else {
      throw new Exception(s"Invalid register: $regStr")
    }
  }

  override def assemble(sourceText: String): String = {
    parallelPcs.clear()
    
    // First Pass: Pre-process lines to find parallel groupings
    val lines = sourceText.split("\n").toSeq
    
    // We want to map cleaned lines to their virtual PCs
    var currentPc = 0
    val cleanedLines = mutable.Buffer[String]()
    
    // First, let's parse labels to get symbol mappings (standard first pass)
    // To do this correctly, we must temporarily strip "||" so parseLabels can calculate instruction sizes
    val strippedLines = lines.map(line => {
      val t = line.trim
      if (t.startsWith("||")) t.substring(2).trim else t
    })
    
    // Resolve labels
    symbols.clear()
    parseLabels(strippedLines)

    // Now, scan the original lines to find where "||" is used
    // If line i starts with "||", and line i-1 was an instruction, then instruction i-1 has p-bit = 1
    var lastInstructionPc = -1
    currentPc = 0
    
    for (line <- lines) {
      val cleaned = cleanLine(line)
      if (cleaned.nonEmpty) {
        val isParallel = cleaned.startsWith("||")
        val stripped = if (isParallel) cleaned.substring(2).trim else cleaned
        
        // Check if this is an ORG or DATA directive, or normal instruction
        val orgMatch = "(?i)^ORG\\s+(.*)".r.findFirstMatchIn(stripped)
        val dataMatch = "(?i)^DATA\\s+(.*)".r.findFirstMatchIn(stripped)
        
        if (orgMatch.isDefined) {
          currentPc = parseNumeric(orgMatch.get.group(1))
        } else if (dataMatch.isDefined) {
          val valsCount = dataMatch.get.group(1).split(",").length
          currentPc += valsCount
        } else {
          // This is a normal instruction at currentPc
          if (isParallel && lastInstructionPc >= 0) {
            parallelPcs += lastInstructionPc
          }
          lastInstructionPc = currentPc
          currentPc += 1
        }
      }
    }

    // Now we assemble as normal using the stripped lines
    // We will hook assembleInstruction to set the p-bit (bit 0) if pc is in parallelPcs
    pc = 0
    val outputValues = mutable.Buffer[Int]()
    
    for (line <- strippedLines) {
      val cleaned = cleanLine(line)
      if (cleaned.nonEmpty) {
        val labelMatch = "^([A-Za-z_][A-Za-z0-9_]*)\\s*:(.*)".r.findFirstMatchIn(cleaned)
        var actualInstruction = cleaned
        var skip = false
        if (labelMatch.isDefined) {
          actualInstruction = labelMatch.get.group(2).trim
          if (actualInstruction.isEmpty) {
            skip = true
          }
        }
        if (!skip) {
          val orgMatch = "(?i)^ORG\\s+(.*)".r.findFirstMatchIn(actualInstruction)
          if (orgMatch.isDefined) {
            pc = parseNumeric(orgMatch.get.group(1))
            while (outputValues.length < pc) {
              outputValues += 0
            }
          } else {
            val dataMatch = "(?i)^DATA\\s+(.*)".r.findFirstMatchIn(actualInstruction)
            if (dataMatch.isDefined) {
              val vals = dataMatch.get.group(1).split(",").map(v => {
                val s = v.trim
                if (symbols.contains(s)) symbols(s) else parseNumeric(s)
              })
              outputValues ++= vals
              pc += vals.length
            } else {
              val assembled = assembleInstruction(actualInstruction)
              // If the PC of this instruction has a parallel successor, set the p-bit (bit 0)
              val updated = assembled.map(inst => {
                if (parallelPcs.contains(pc)) {
                  inst | 1
                } else {
                  inst & ~1
                }
              })
              outputValues ++= updated
              pc += 1
            }
          }
        }
      }
    }
    formatOutput(outputValues.toSeq)
  }

  override def assembleInstruction(line: String): Seq[Int] = {
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return Seq()

    val parts = s.split(" ", 2)
    val op = parts(0).toUpperCase
    
    if (op == "HLT") {
      return Seq(5 << 28)
    }

    if (op == "JMP") {
      val target = parseNumericOrSymbol(parts(1))
      return Seq((6 << 28) | (target & 0xFFFF) << 1)
    }

    if (op == "LD_CU") {
      val operands = parts(1).split(",")
      val rd = parseReg(operands(0))
      val target = parseNumericOrSymbol(operands(1))
      return Seq((7 << 28) | (rd << 24) | (target & 0xFFFF) << 1)
    }

    val operands = parts(1).split(",")
    val opCode = op match {
      case "LDW" => 1
      case "STW" => 2
      case "ADD" => 3
      case "SUB" => 4
      case _     => throw new Exception(s"Unknown TMS320C6k op: $op")
    }

    if (op == "LDW") {
      // Format: LDW *Rs, Rd
      val rsStr = operands(0).trim.stripPrefix("*")
      val rs = parseReg(rsStr)
      val rd = parseReg(operands(1))
      Seq((opCode << 28) | (rd << 24) | (rs << 20))
    } else if (op == "STW") {
      // Format: STW Rs, *Rd
      val rs = parseReg(operands(0))
      val rdStr = operands(1).trim.stripPrefix("*")
      val rd = parseReg(rdStr)
      Seq((opCode << 28) | (rs << 24) | (rd << 20))
    } else {
      // Format: ADD Rs1, Rs2, Rd or SUB Rs1, Rs2, Rd
      val rs1 = parseReg(operands(0))
      val rs2 = parseReg(operands(1))
      val rd = parseReg(operands(2))
      Seq((opCode << 28) | (rd << 24) | (rs1 << 20) | (rs2 << 16))
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled TMS320C6k Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
