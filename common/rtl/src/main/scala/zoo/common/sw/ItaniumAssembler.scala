package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class ItaniumAssembler extends BaseAssembler with GeneralRegisterArchitecture with VariableLengthFormat with FixedPointOperations {
  override val wordWidth: Int = 32
  override val addressWidth: Int = 32
  override val numGPRs: Int = 16
  override val numFPRs: Int = 0
  override val opCodeWidth: Int = 4
  override val formats = Seq("Single")
  override val hasMultiplyDivide: Boolean = false
  commentPattern = "(#|//).*"

  override def getArchInstructionSize(line: String): Int = {
    val s = line.trim
    if (s.isEmpty) return 0
    // Every bundle is 128-bit (4 words of 32-bit)
    // In our simplified parser, we will pre-group them
    4
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
    // Resolve labels first
    val rawLines = sourceText.split("\n").toSeq
    val strippedLines = rawLines.map(_.trim).filterNot(l => l.isEmpty || l.startsWith("#"))
    
    // Pass 1: Build symbols (each EPIC bundle is 4 words)
    symbols.clear()
    pc = 0
    for (line <- strippedLines) {
      val orgMatch = "(?i)^ORG\\s+(.*)".r.findFirstMatchIn(line)
      val dataMatch = "(?i)^DATA\\s+(.*)".r.findFirstMatchIn(line)
      val labelMatch = "^([A-Za-z_][A-Za-z0-9_]*)\\s*:(.*)".r.findFirstMatchIn(line)

      if (orgMatch.isDefined) {
        pc = parseNumeric(orgMatch.get.group(1))
      } else if (dataMatch.isDefined) {
        pc += dataMatch.get.group(1).split(",").length
      } else if (labelMatch.isDefined) {
        val label = labelMatch.get.group(1)
        symbols(label) = pc
        val rest = labelMatch.get.group(2).trim
        if (rest.startsWith("{")) {
          // It's the start of a bundle
          pc += 4
        } else if (rest.nonEmpty && !rest.startsWith("}") && !rest.startsWith(".")) {
          // If it has standard data or instruction
        }
      } else if (line.startsWith("{")) {
        pc += 4
      }
    }

    // Pass 2: Assemble bundles
    pc = 0
    val outputValues = mutable.Buffer[Int]()
    
    var inBundle = false
    var bundleTemplate = 0
    val bundleSlots = mutable.Buffer[String]()
    val stopBits = mutable.Set[Int]()

    for (line <- strippedLines) {
      val labelMatch = "^([A-Za-z_][A-Za-z0-9_]*)\\s*:(.*)".r.findFirstMatchIn(line)
      val content = if (labelMatch.isDefined) labelMatch.get.group(2).trim else line

      if (content.nonEmpty) {
        val orgMatch = "(?i)^ORG\\s+(.*)".r.findFirstMatchIn(content)
        val dataMatch = "(?i)^DATA\\s+(.*)".r.findFirstMatchIn(content)
        
        if (orgMatch.isDefined) {
          pc = parseNumeric(orgMatch.get.group(1))
          while (outputValues.length < pc) {
            outputValues += 0
          }
        } else if (dataMatch.isDefined) {
          val vals = dataMatch.get.group(1).split(",").map(v => {
            val s = v.trim
            if (symbols.contains(s)) symbols(s) else parseNumeric(s)
          })
          outputValues ++= vals
          pc += vals.length
        } else if (content.startsWith("{")) {
          inBundle = true
          bundleSlots.clear()
          stopBits.clear()
          
          val rest = content.stripPrefix("{").trim
          if (rest.nonEmpty) {
            // Check if template is on the same line
            if (rest.startsWith(".")) {
              bundleTemplate = if (rest.startsWith(".mmi")) 1 else 2
            } else {
              parseSlot(rest, bundleSlots, stopBits)
            }
          }
        } else if (content.startsWith("}")) {
          // End of bundle, compile and pad
          while (bundleSlots.length < 3) {
            bundleSlots += "nop 0"
          }
          
          val w0 = compileItaniumSlot(bundleSlots(0))
          val w1 = compileItaniumSlot(bundleSlots(1))
          val w2 = compileItaniumSlot(bundleSlots(2))
          
          var templateWord = bundleTemplate
          if (stopBits.contains(0)) templateWord |= (1 << 8)
          if (stopBits.contains(1)) templateWord |= (1 << 9)
          if (stopBits.contains(2)) templateWord |= (1 << 10)
          
          outputValues += w0
          outputValues += w1
          outputValues += w2
          outputValues += templateWord
          
          pc += 4
          inBundle = false
        } else if (inBundle) {
          if (content.startsWith(".")) {
            bundleTemplate = if (content.startsWith(".mmi")) 1 else 2
          } else {
            parseSlot(content, bundleSlots, stopBits)
          }
        }
      }
    }
    
    formatOutput(outputValues.toSeq)
  }

  private def parseSlot(text: String, slots: mutable.Buffer[String], stopBits: mutable.Set[Int]): Unit = {
    var cleanText = text
    val idx = slots.length
    if (cleanText.endsWith(";;")) {
      stopBits += idx
      cleanText = cleanText.substring(0, cleanText.length - 2).trim
    }
    slots += cleanText
  }

  private def compileItaniumSlot(inst: String): Int = {
    val s = inst.trim.replaceAll("\\s+", " ")
    if (s.isEmpty || s.toUpperCase == "NOP 0" || s.toUpperCase == "NOP") return 0

    if (s.toUpperCase == "HLT") return 7 << 24

    val parts = s.split(" ", 2)
    val op = parts(0).toLowerCase

    if (op == "jmp") {
      val target = parseNumericOrSymbol(parts(1))
      return (6 << 24) | (target & 0xFFFF)
    }

    val operands = parts(1).split("=")
    val dest = operands(0).trim
    val src = if (operands.length > 1) operands(1).trim else ""

    if (op == "ld8") {
      // ld8 rd = [rs]
      val rd = parseReg(dest)
      val rsStr = src.stripPrefix("[").stripSuffix("]")
      val rs = parseReg(rsStr)
      return (1 << 24) | (rd << 16) | (rs << 8)
    } else if (op == "st8") {
      // st8 [rd] = rs
      val rdStr = dest.stripPrefix("[").stripSuffix("]")
      val rd = parseReg(rdStr)
      val rs = parseReg(src)
      return (2 << 24) | (rd << 16) | (rs << 8)
    } else if (op == "ld_cu") {
      // ld_cu rd = target
      val rd = parseReg(dest)
      val target = parseNumericOrSymbol(src)
      return (5 << 24) | (rd << 16) | (target & 0xFFFF)
    } else {
      // add rd = rs1, rs2 or sub rd = rs1, rs2
      val rd = parseReg(dest)
      val srcParts = src.split(",")
      val rs1 = parseReg(srcParts(0))
      val rs2 = parseReg(srcParts(1))
      val opCode = op match {
        case "add" => 3
        case "sub" => 4
        case _     => throw new Exception(s"Unknown Itanium op: $op")
      }
      return (opCode << 24) | (rd << 16) | (rs1 << 8) | rs2
    }
  }

  override def assembleInstruction(line: String): Seq[Int] = Seq()

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Itanium Hex File"
    for (v <- values) {
      out += f"${v & 0xFFFFFFFFL}%08X"
    }
    out.mkString("\n") + "\n"
  }
}
