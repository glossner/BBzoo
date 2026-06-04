package zoo.common.sw

import java.io.PrintWriter
import scala.io.Source

object Assembler {
  def main(args: Array[String]): Unit = {
    if (args.length < 5 || args(0) != "-arch") {
      println("Usage: sbt \"common/runMain zoo.common.sw.Assembler -arch <pdp8|ibm360|cray1|m68k|burroughsb5500|decpdp11|cdc6600|mos6502|babbage|harvardmark1|zusez1|manchestermu1|univac1|princetonias|cambridgeedsac|ibm701|ibm704|ibm650|ibm705|ibm1401|stczebra|bullgamma60|ibmstretch|univac1103a|cdc6600ppu|decvax|intel8080a|motorola6800|ibm6150|mips1|arm1|berkeleyrisc|hp3000|ethlilith|ucsdp|upd7720|tms32010|adsp2100|ibmmwave|voodoo1|geforce256|radeonr100|powervr1|mali200|amdr600> <input.asm> -o <output.hex>\"")
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
      case "manchestermu1"   => new Manchestermu1Assembler()
      case "univac1"         => new Univac1Assembler()
      case "princetonias"    => new PrincetoniasAssembler()
      case "cambridgeedsac"  => new CambridgeedsacAssembler()
      case "ibm701"          => new Ibm701Assembler()
      case "ibm704"          => new Ibm704Assembler()
      case "ibm650"          => new Ibm650Assembler()
      case "ibm705"          => new Ibm705Assembler()
      case "ibm1401"         => new Ibm1401Assembler()
      case "stczebra"        => new StczebraAssembler()
      case "bullgamma60"     => new Bullgamma60Assembler()
      case "ibmstretch"      => new IbmstretchAssembler()
      case "univac1103a"     => new Univac1103aAssembler()
      case "cdc6600ppu"      => new Cdc6600ppuAssembler()
      case "decvax"          => new DecvaxAssembler()
      case "intel8080a"      => new Intel8080aAssembler()
      case "motorola6800"    => new Motorola6800Assembler()
      case "ibm6150"         => new Ibm6150Assembler()
      case "mips1"           => new Mips1Assembler()
      case "arm1"            => new Arm1Assembler()
      case "berkeleyrisc"    => new BerkeleyriscAssembler()
      case "hp3000"          => new Hp3000Assembler()
      case "ethlilith"       => new EthlilithAssembler()
      case "ucsdp"           => new UcsdpAssembler()
      case "upd7720"         => new Upd7720Assembler()
      case "tms32010"        => new Tms32010Assembler()
      case "adsp2100"        => new Adsp2100Assembler()
      case "ibmmwave"        => new IbmmwaveAssembler()
      case "voodoo1"         => new Voodoo1Assembler()
      case "geforce256"      => new Geforce256Assembler()
      case "radeonr100"      => new Radeonr100Assembler()
      case "powervr1"        => new Powervr1Assembler()
      case "mali200"         => new Mali200Assembler()
      case "amdr600"         => new Amdr600Assembler()
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
