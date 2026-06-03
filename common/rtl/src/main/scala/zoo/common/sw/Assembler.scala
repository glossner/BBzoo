package zoo.common.sw

import java.io.PrintWriter
import scala.io.Source

object Assembler {
  def main(args: Array[String]): Unit = {
    if (args.length < 5 || args(0) != "-arch") {
      println("Usage: sbt \"common/runMain zoo.common.sw.Assembler -arch <pdp8|ibm360|cray1|m68k> <input.asm> -o <output.hex>\"")
      sys.exit(1)
    }

    val arch = args(1)
    val input = args(2)
    val output = args(4)

    val assembler = arch match {
      case "pdp8"   => new Pdp8Assembler()
      case "ibm360" => new Ibm360Assembler()
      case "cray1"  => new Cray1Assembler()
      case "m68k"   => new M68kAssembler()
      case _ =>
        println(s"Unknown architecture: $arch")
        sys.exit(1)
    }

    try {
      val source = Source.fromFile(input).mkString
      val hex = assembler.assemble(source)
      val pw = new PrintWriter(output)
      pw.write(hex)
      pw.close()
      println(s"Successfully assembled $input to $output")
    } catch {
      case e: Exception =>
        println(s"Error assembling file: ${e.getMessage}")
        e.printStackTrace()
        sys.exit(1)
    }
  }
}
