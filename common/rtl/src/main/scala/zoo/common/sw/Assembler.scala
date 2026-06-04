package zoo.common.sw

import java.io.PrintWriter
import scala.io.Source

object Assembler {
  def main(args: Array[String]): Unit = {
    if (args.length < 5 || args(0) != "-arch") {
      println("Usage: sbt \"common/runMain zoo.common.sw.Assembler -arch <pdp8|ibm360|cray1|m68k|burroughsb5500|decpdp11|cdc6600|mos6502|babbage|harvardmark1|zusez1|manchester|univac1|ias|edsac|ibm701|ibm704|ibm650|ibm705|ibm1401|stczebra|bullgamma60|ibmstretch> <input.asm> -o <output.hex>\"")
      sys.exit(1)
    }

    val arch = args(1)
    val input = args(2)
    val output = args(4)

    val assembler = arch match {
      case "pdp8"            => new Pdp8Assembler()
      case "ibm360"          => new Ibm360Assembler()
      case "cray1"           => new Cray1Assembler()
      case "m68k"            => new M68kAssembler()
      case "burroughsb5500"  => new B5500Assembler()
      case "decpdp11"        => new Pdp11Assembler()
      case "cdc6600"         => new Cdc6600Assembler()
      case "mos6502"         => new Mos6502Assembler()
      case "babbage"         => new BabbageAssembler()
      case "harvardmark1"    => new HarvardMark1Assembler()
      case "zusez1"          => new ZuseZ1Assembler()
      case "manchester"      => new ManchesterAssembler()
      case "univac1"         => new Univac1Assembler()
      case "ias"             => new IasAssembler()
      case "edsac"           => new EdsacAssembler()
      case "ibm701"          => new Ibm701Assembler()
      case "ibm704"          => new Ibm704Assembler()
      case "ibm650"          => new Ibm650Assembler()
      case "ibm705"          => new Ibm705Assembler()
      case "ibm1401"         => new Ibm1401Assembler()
      case "stczebra"        => new StczebraAssembler()
      case "bullgamma60"     => new Bullgamma60Assembler()
      case "ibmstretch"      => new IbmstretchAssembler()
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
