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
  verifyHex("manchester", "manchester/sw/test_vector.asm", "manchester/sw/test_vector.hex", new ManchesterAssembler())
  verifyHex("univac1", "univac1/sw/test_vector.asm", "univac1/sw/test_vector.hex", new Univac1Assembler())
  verifyHex("ias", "ias/sw/test_vector.asm", "ias/sw/test_vector.hex", new IasAssembler())
  verifyHex("edsac", "edsac/sw/test_vector.asm", "edsac/sw/test_vector.hex", new EdsacAssembler())
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
}


