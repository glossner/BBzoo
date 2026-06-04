package zoo.common.sw

import scala.collection.mutable
import zoo.common.architecture._

abstract class BaseAssembler extends StorageArchitecture with InstructionFormat with Operations {
  val symbols = mutable.Map[String, Int]()
  var pc = 0
  var commentPattern = "(#|;|//).*"

  def cleanLine(line: String): String = {
    // Remove comments starting with # at start of line
    val noLineHash = if (line.trim.startsWith("#")) "" else line
    // Remove comments based on commentPattern
    noLineHash.replaceAll(commentPattern, "").trim
  }

  def parseLabels(lines: Seq[String]): Seq[String] = {
    pc = 0
    val cleanedLines = mutable.Buffer[String]()
    for (line <- lines) {
      val cleaned = cleanLine(line)
      if (cleaned.nonEmpty) {
        val labelMatch = "^([A-Za-z_][A-Za-z0-9_]*)\\s*:(.*)".r.findFirstMatchIn(cleaned)
        if (labelMatch.isDefined) {
          val label = labelMatch.get.group(1)
          symbols(label) = pc
          val rest = labelMatch.get.group(2).trim
          if (rest.nonEmpty) {
            cleanedLines += rest
            pc += getInstructionSize(rest)
          }
        } else {
          val orgMatch = "(?i)^ORG\\s+(.*)".r.findFirstMatchIn(cleaned)
          if (orgMatch.isDefined) {
            pc = parseNumeric(orgMatch.get.group(1))
            cleanedLines += cleaned
          } else {
            cleanedLines += cleaned
            pc += getInstructionSize(cleaned)
          }
        }
      }
    }
    cleanedLines.toSeq
  }

  def parseNumeric(valStr: String): Int = {
    val s = valStr.trim
    val isNegative = s.startsWith("-")
    val absStr = if (isNegative || s.startsWith("+")) s.substring(1) else s

    val res = if (absStr.toLowerCase.startsWith("0x")) {
      Integer.parseInt(absStr.substring(2), 16)
    } else if (absStr.toLowerCase.startsWith("0o")) {
      Integer.parseInt(absStr.substring(2), 8)
    } else if (absStr.toLowerCase.startsWith("0b")) {
      Integer.parseInt(absStr.substring(2), 2)
    } else if (absStr.startsWith("0") && absStr.length > 1 && absStr.forall(_.isDigit)) {
      Integer.parseInt(absStr, 8)
    } else {
      absStr.toInt
    }
    if (isNegative) -res else res
  }

  def getInstructionSize(line: String): Int = {
    val s = line.trim
    if ("(?i)^ORG\\s+".r.findFirstIn(s).isDefined) {
      0
    } else {
      val dataMatch = "(?i)^DATA\\s+(.*)".r.findFirstMatchIn(s)
      if (dataMatch.isDefined) {
        dataMatch.get.group(1).split(",").length
      } else {
        getArchInstructionSize(s)
      }
    }
  }

  def getArchInstructionSize(line: String): Int
  def assembleInstruction(line: String): Seq[Int]
  def formatOutput(values: Seq[Int]): String

  def assemble(sourceText: String): String = {
    val lines = sourceText.split("\n").toSeq
    val instructions = parseLabels(lines)

    pc = 0
    val outputValues = mutable.Buffer[Int]()
    for (line <- instructions) {
      val orgMatch = "(?i)^ORG\\s+(.*)".r.findFirstMatchIn(line)
      if (orgMatch.isDefined) {
        pc = parseNumeric(orgMatch.get.group(1))
        while (outputValues.length < pc) {
          outputValues += 0
        }
      } else {
        val dataMatch = "(?i)^DATA\\s+(.*)".r.findFirstMatchIn(line)
        if (dataMatch.isDefined) {
          val vals = dataMatch.get.group(1).split(",").map(v => {
            val s = v.trim
            if (symbols.contains(s)) symbols(s) else parseNumeric(s)
          })
          outputValues ++= vals
          pc += vals.length
        } else {
          val assembled = assembleInstruction(line)
          outputValues ++= assembled
          pc += getInstructionSize(line)
        }
      }
    }
    formatOutput(outputValues.toSeq)
  }
}
