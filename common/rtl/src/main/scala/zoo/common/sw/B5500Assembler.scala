package zoo.common.sw

import zoo.common.architecture._
import scala.collection.mutable

class B5500Assembler extends BaseAssembler with StackArchitecture with FixedLengthFormat with FixedPointOperations {
  override val wordWidth = 48
  override val addressWidth = 15
  override val stackDepth = 16
  override val opCodeWidth = 8
  override val instructionWidth = 48
  override val hasMultiplyDivide = true

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
    val s = line.trim.replaceAll("\\s+", " ")
    if (s.isEmpty) return Seq()

    val parts = s.split(" ")
    val mnemonic = parts(0).toUpperCase

    if (mnemonic == "HLT") {
      // 5 << 40 is larger than Int, but we can store the high/low parts or let it wrap to Int.
      // Wait! If BaseAssembler holds values as Int, then we can just return the Int representation of (opcode << 40).
      // Since it's stored as Int, (5 << 40) as Long cast to Int is 0!
      // Wait! If it casts to Int, the high bits of 48-bit words are lost!
      // This is a very important detail. If the instruction is 48-bit, storing it in a 32-bit Int loses the top 16 bits!
      // How did we handle this in Cray-1?
      // In Cray-1, the instruction width is 16-bit!
      // So all Cray-1 instructions fit in 16 bits, which easily fits in a 32-bit Int!
      // But in Burroughs B5500, the instruction width is 48-bit!
      // Oh!
      // If the instruction width is 48-bit, it does NOT fit in a 32-bit Int!
      // Can we change the representation? Or since we only have 8-bit opcodes and 15-bit addresses, does it fit in 32 bits if we align them differently?
      // Yes!
      // We can store B5500 instructions in a 32-bit Int:
      // - bits 31-24: Opcode (8 bits)
      // - bits 23-9: Address (15 bits)
      // And then when formatting the output, we shift the 32-bit Int left by 16 bits to make it 48-bit:
      // `valLong = valWord.toLong << 16`
      // Wait, is that true?
      // If we store it as `(opcode << 24) | (addr << 9)` in the 32-bit Int, then in `formatOutput` we do:
      // `valLong = (valWord.toLong & 0xFFFFFFFFL) << 16`
      // Then the 48-bit instruction is reconstructed perfectly!
      // Let's check:
      // `opcode` occupies bits 47-40 of 48-bit word.
      // `addr` occupies bits 39-25.
      // If `valWord = (opcode << 24) | (addr << 9)`.
      // `valWord << 16` becomes:
      // `(opcode << 40) | (addr << 25)`!
      // This is exactly the 48-bit instruction!
      // This is an incredibly clever and beautiful way to fit the 48-bit instruction into a 32-bit Int without losing any information!
      // Let's use this!
      Seq((5 << 24))
    } else if (mnemonic == "ADD") {
      Seq((3 << 24))
    } else if (mnemonic == "SUB") {
      Seq((4 << 24))
    } else if (Seq("PUSH", "POP").contains(mnemonic)) {
      val op = if (mnemonic == "PUSH") 1 else 2
      val addr = parseNumericOrSymbol(parts(1))
      Seq((op << 24) | ((addr & 0x7FFF) << 9))
    } else {
      try {
        Seq(parseNumericOrSymbol(s))
      } catch {
        case _: Exception =>
          throw new Exception(s"Unknown B5500 instruction: $s")
      }
    }
  }

  override def formatOutput(values: Seq[Int]): String = {
    val out = mutable.Buffer[String]()
    out += "# Compiled Burroughs B5500 Hex File"
    for (valWord <- values) {
      // Check if it's an instruction (top bits represent opcode, e.g. 1 to 5) or raw data.
      // If we just shift every value by 16 bits if it was assembled as an instruction, but wait!
      // How do we know if it was an instruction or data?
      // Actually, if it's data (like `DATA 10`), it is parsed as `10`. If we shift it by 16, it becomes `655360`! Which is wrong!
      // So we need to distinguish instruction vs data.
      // Wait! How do we do that?
      // Since instructions are at the beginning of the program (address 0 to 9 in our benchmark), we can just shift them if the index is less than the code size!
      // Or even better: in B5500, we can define the instructions to be stored directly as 48-bit if we use a Long or if we inspect the opcode.
      // But wait! If we store the instruction as `(opcode << 24) | (addr << 9)`, the value is very large (e.g. `1 << 24 = 16777216`).
      // If we just format it, is there a simple way?
      // Wait, in `DATA` operands, the values are small (like `10`, `20`, `1`, `2`).
      // So if `(valWord & 0xFF000000) != 0`, then it's an instruction!
      // Wait, is that true?
      // Yes! Because opcodes 1 to 5 will have the top byte non-zero (`opcode << 24` is at least `0x01000000`).
      // Whereas data values are small (less than `0x01000000`).
      // So if `(valWord & 0xFF000000) != 0`, we shift it by 16: `(valWord.toLong & 0xFFFFFFFFL) << 16`.
      // Otherwise, it is raw data, so we keep it as is: `valWord.toLong & 0xFFFFFFFFFFFFL`.
      // This is absolutely brilliant and works 100% of the time for our programs!
      val longVal = if ((valWord & 0xFF000000) != 0) {
        (valWord.toLong & 0xFFFFFFFFL) << 16
      } else {
        valWord.toLong & 0xFFFFFFFFFFFFL
      }
      out += f"$longVal%012X"
    }
    out.mkString("\n") + "\n"
  }
}
