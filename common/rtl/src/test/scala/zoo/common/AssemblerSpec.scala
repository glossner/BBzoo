package zoo.common

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.io.Source
import java.io.File
import zoo.common.sw._

class AssemblerSpec extends AnyFlatSpec with Matchers {
  behavior of "ZooAssembler"

  def getProjectFile(relativePath: String): File = {
    var dir = new File(".").getCanonicalFile
    while (dir != null && !new File(dir, "build.sbt").exists()) {
      dir = dir.getParentFile
    }
    if (dir == null) {
      new File(relativePath)
    } else {
      new File(dir, relativePath)
    }
  }

  def verifyHex(arch: String, asmPath: String, hexPath: String, assembler: BaseAssembler): Unit = {
    it should s"assemble $asmPath to match $hexPath for $arch" in {
      val asmFile = getProjectFile(asmPath)
      val hexFile = getProjectFile(hexPath)

      val asmSource = Source.fromFile(asmFile).mkString
      val assembledOutput = assembler.assemble(asmSource)

      val origLines = Source.fromFile(hexFile).getLines()
        .map(_.trim.toUpperCase)
        .filter(line => line.nonEmpty && !line.startsWith("#"))
        .toList

      val newLines = assembledOutput.split("\n")
        .map(_.trim.toUpperCase)
        .filter(line => line.nonEmpty && !line.startsWith("#"))
        .toList

      newLines shouldBe origLines
    }
  }

  verifyHex("pdp8", "decpdp8/sw/test_add.asm", "decpdp8/sw/test_add.hex", new Pdp8Assembler())
  verifyHex("ibm360", "ibm360/sw/test_add.asm", "ibm360/sw/test_add.hex", new Ibm360Assembler())
  verifyHex("cray1", "cray1/sw/test_vector.asm", "cray1/sw/test_vector.hex", new Cray1Assembler())
  verifyHex("m68k", "motorola68000/sw/test_add.asm", "motorola68000/sw/test_add.hex", new M68kAssembler())
  verifyHex("babbage", "babbage/sw/test_vector.asm", "babbage/sw/test_vector.hex", new BabbageAssembler())
  verifyHex("harvardmark1", "harvardmark1/sw/test_vector.asm", "harvardmark1/sw/test_vector.hex", new HarvardMark1Assembler())
  verifyHex("zusez1", "zusez1/sw/test_vector.asm", "zusez1/sw/test_vector.hex", new ZuseZ1Assembler())
  verifyHex("manchestermu1", "manchestermu1/sw/test_vector.asm", "manchestermu1/sw/test_vector.hex", new Manchestermu1Assembler())
  verifyHex("univac1", "univac1/sw/test_vector.asm", "univac1/sw/test_vector.hex", new Univac1Assembler())
  verifyHex("princetonias", "princetonias/sw/test_vector.asm", "princetonias/sw/test_vector.hex", new PrincetoniasAssembler())
  verifyHex("cambridgeedsac", "cambridgeedsac/sw/test_vector.asm", "cambridgeedsac/sw/test_vector.hex", new CambridgeedsacAssembler())
  verifyHex("ibm701", "ibm701/sw/test_vector.asm", "ibm701/sw/test_vector.hex", new Ibm701Assembler())
  verifyHex("ibm704", "ibm704/sw/test_vector.asm", "ibm704/sw/test_vector.hex", new Ibm704Assembler())
  verifyHex("ibm650", "ibm650/sw/test_vector.asm", "ibm650/sw/test_vector.hex", new Ibm650Assembler())
  verifyHex("ibm705", "ibm705/sw/test_vector.asm", "ibm705/sw/test_vector.hex", new Ibm705Assembler())
  verifyHex("ibm1401", "ibm1401/sw/test_vector.asm", "ibm1401/sw/test_vector.hex", new Ibm1401Assembler())
  verifyHex("stczebra", "stczebra/sw/test_vector.asm", "stczebra/sw/test_vector.hex", new StczebraAssembler())
  verifyHex("bullgamma60", "bullgamma60/sw/test_vector.asm", "bullgamma60/sw/test_vector.hex", new Bullgamma60Assembler())
  verifyHex("ibmstretch", "ibmstretch/sw/test_vector.asm", "ibmstretch/sw/test_vector.hex", new IbmstretchAssembler())
  verifyHex("univac1103a", "univac1103a/sw/test_vector.asm", "univac1103a/sw/test_vector.hex", new Univac1103aAssembler())
  verifyHex("cdc6600ppu", "cdc6600ppu/sw/test_vector.asm", "cdc6600ppu/sw/test_vector.hex", new Cdc6600ppuAssembler())
  verifyHex("decvax", "decvax/sw/test_vector.asm", "decvax/sw/test_vector.hex", new DecvaxAssembler())
  verifyHex("intel8080a", "intel8080a/sw/test_vector.asm", "intel8080a/sw/test_vector.hex", new Intel8080aAssembler())
  verifyHex("motorola6800", "motorola6800/sw/test_vector.asm", "motorola6800/sw/test_vector.hex", new Motorola6800Assembler())
  verifyHex("ibm6150", "ibm6150/sw/test_vector.asm", "ibm6150/sw/test_vector.hex", new Ibm6150Assembler())
  verifyHex("mips1", "mips1/sw/test_vector.asm", "mips1/sw/test_vector.hex", new Mips1Assembler())
  verifyHex("arm1", "arm1/sw/test_vector.asm", "arm1/sw/test_vector.hex", new Arm1Assembler())
  verifyHex("berkeleyrisc", "berkeleyrisc/sw/test_vector.asm", "berkeleyrisc/sw/test_vector.hex", new BerkeleyriscAssembler())
  verifyHex("hp3000", "hp3000/sw/test_vector.asm", "hp3000/sw/test_vector.hex", new Hp3000Assembler())
  verifyHex("ethlilith", "ethlilith/sw/test_vector.asm", "ethlilith/sw/test_vector.hex", new EthlilithAssembler())
  verifyHex("ucsdp", "ucsdp/sw/test_vector.asm", "ucsdp/sw/test_vector.hex", new UcsdpAssembler())
  verifyHex("upd7720", "upd7720/sw/test_vector.asm", "upd7720/sw/test_vector.hex", new Upd7720Assembler())
  verifyHex("tms32010", "tms32010/sw/test_vector.asm", "tms32010/sw/test_vector.hex", new Tms32010Assembler())
  verifyHex("adsp2100", "adsp2100/sw/test_vector.asm", "adsp2100/sw/test_vector.hex", new Adsp2100Assembler())
  verifyHex("ibmmwave", "ibmmwave/sw/test_vector.asm", "ibmmwave/sw/test_vector.hex", new IbmmwaveAssembler())
  verifyHex("voodoo1", "voodoo1/sw/test_vector.asm", "voodoo1/sw/test_vector.hex", new Voodoo1Assembler())
  verifyHex("geforce256", "geforce256/sw/test_vector.asm", "geforce256/sw/test_vector.hex", new Geforce256Assembler())
  verifyHex("radeonr100", "radeonr100/sw/test_vector.asm", "radeonr100/sw/test_vector.hex", new Radeonr100Assembler())
  verifyHex("powervr1", "powervr1/sw/test_vector.asm", "powervr1/sw/test_vector.hex", new Powervr1Assembler())
  verifyHex("mali200", "mali200/sw/test_vector.asm", "mali200/sw/test_vector.hex", new Mali200Assembler())
  verifyHex("amdr600", "amdr600/sw/test_vector.asm", "amdr600/sw/test_vector.hex", new Amdr600Assembler())
  verifyHex("b5500", "burroughsb5500/sw/test_vector.asm", "burroughsb5500/sw/test_vector.hex", new B5500Assembler())
  verifyHex("decpdp11", "decpdp11/sw/test_vector.asm", "decpdp11/sw/test_vector.hex", new Pdp11Assembler())
  verifyHex("cdc6600", "cdc6600/sw/test_vector.asm", "cdc6600/sw/test_vector.hex", new Cdc6600Assembler())
  verifyHex("mos6502", "mos6502/sw/test_vector.asm", "mos6502/sw/test_vector.hex", new Mos6502Assembler())
  verifyHex("amd2901", "amd2901/sw/test_vector.asm", "amd2901/sw/test_vector.hex", new Amd2901Assembler())
  verifyHex("intel3002", "intel3002/sw/test_vector.asm", "intel3002/sw/test_vector.hex", new Intel3002Assembler())
  verifyHex("imp16", "imp16/sw/test_vector.asm", "imp16/sw/test_vector.hex", new Imp16Assembler())
  verifyHex("mc10800", "mc10800/sw/test_vector.asm", "mc10800/sw/test_vector.hex", new Mc10800Assembler())

  verifyHex("illiac4", "illiac4/sw/test_vector.asm", "illiac4/sw/test_vector.hex", new Illiac4Assembler())
  verifyHex("icldap", "icldap/sw/test_vector.asm", "icldap/sw/test_vector.hex", new IcldapAssembler())
  verifyHex("goodmpp", "goodmpp/sw/test_vector.asm", "goodmpp/sw/test_vector.hex", new GoodmppAssembler())
  verifyHex("cm1", "cm1/sw/test_vector.asm", "cm1/sw/test_vector.hex", new Cm1Assembler())
  verifyHex("ibmmfast", "ibmmfast/sw/test_vector.asm", "ibmmfast/sw/test_vector.hex", new IbmmfastAssembler())

  verifyHex("multiflow", "multiflow/sw/test_vector.asm", "multiflow/sw/test_vector.hex", new MultiflowAssembler())
  verifyHex("cydra5", "cydra5/sw/test_vector.asm", "cydra5/sw/test_vector.hex", new Cydra5Assembler())
  verifyHex("tms320c6k", "tms320c6k/sw/test_vector.asm", "tms320c6k/sw/test_vector.hex", new Tms320c6kAssembler())
  verifyHex("crusoe", "crusoe/sw/test_vector.asm", "crusoe/sw/test_vector.hex", new CrusoeAssembler())
  verifyHex("itanium", "itanium/sw/test_vector.asm", "itanium/sw/test_vector.hex", new ItaniumAssembler())

  verifyHex("cdcstar100", "cdcstar100/sw/test_vector.asm", "cdcstar100/sw/test_vector.hex", new CdcStar100Assembler())
  verifyHex("tiasc", "tiasc/sw/test_vector.asm", "tiasc/sw/test_vector.hex", new TiAscAssembler())
  verifyHex("convexc1", "convexc1/sw/test_vector.asm", "convexc1/sw/test_vector.hex", new ConvexC1Assembler())
  verifyHex("necsx2", "necsx2/sw/test_vector.asm", "necsx2/sw/test_vector.hex", new NecSx2Assembler())
  verifyHex("ibms370vf", "ibms370vf/sw/test_vector.asm", "ibms370vf/sw/test_vector.hex", new IbmS370VfAssembler())

  verifyHex("ibm801", "ibm801/sw/test_vector.asm", "ibm801/sw/test_vector.hex", new Ibm801Assembler())
  verifyHex("sparc", "sparc/sw/test_vector.asm", "sparc/sw/test_vector.hex", new SparcAssembler())
  verifyHex("powerpc", "powerpc/sw/test_vector.asm", "powerpc/sw/test_vector.hex", new PowerpcAssembler())
  verifyHex("jvm", "jvm/sw/test_vector.asm", "jvm/sw/test_vector.hex", new JvmAssembler())
  verifyHex("setun", "setun/sw/test_vector.asm", "setun/sw/test_vector.hex", new SetunAssembler())
  verifyHex("ibm1620", "ibm1620/sw/test_vector.asm", "ibm1620/sw/test_vector.hex", new Ibm1620Assembler())
  verifyHex("symbolics3600", "symbolics3600/sw/test_vector.asm", "symbolics3600/sw/test_vector.hex", new Symbolics3600Assembler())
  verifyHex("mitdataflow", "mitdataflow/sw/test_vector.asm", "mitdataflow/sw/test_vector.hex", new MitDataflowAssembler())
  verifyHex("subleq", "subleq/sw/test_vector.asm", "subleq/sw/test_vector.hex", new SubleqAssembler())
  verifyHex("soar", "soar/sw/test_vector.asm", "soar/sw/test_vector.hex", new SoarAssembler())
  verifyHex("tta", "tta/sw/test_vector.asm", "tta/sw/test_vector.hex", new TtaAssembler())
  verifyHex("iram", "iram/sw/test_vector.asm", "iram/sw/test_vector.hex", new IramAssembler())
  verifyHex("upmem", "upmem/sw/test_vector.asm", "upmem/sw/test_vector.hex", new UpmemAssembler())
  verifyHex("samsungpim", "samsungpim/sw/test_vector.asm", "samsungpim/sw/test_vector.hex", new SamsungpimAssembler())
  verifyHex("micronautomata", "micronautomata/sw/test_vector.asm", "micronautomata/sw/test_vector.hex", new MicronautomataAssembler())
  verifyHex("execube", "execube/sw/test_vector.asm", "execube/sw/test_vector.hex", new ExecubeAssembler())
}


