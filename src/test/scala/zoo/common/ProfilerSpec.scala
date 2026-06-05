package zoo.common

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.io.Source
import java.io.File

import zoo.decpdp8.Pdp8Core
import zoo.ibm360.Ibm360Core
import zoo.cray1.Cray1Core
import zoo.motorola68000.M68kCore
import zoo.burroughsb5500.B5500Core
import zoo.decpdp11.Pdp11Core
import zoo.cdc6600.Cdc6600Core
import zoo.mos6502.Mos6502Core
import zoo.babbage.BabbageCore
import zoo.harvardmark1.HarvardMark1Core
import zoo.zusez1.ZuseZ1Core
import zoo.manchestermu1.Manchestermu1Core
import zoo.univac1.Univac1Core
import zoo.princetonias.PrincetoniasCore
import zoo.cambridgeedsac.CambridgeedsacCore
import zoo.ibm701.Ibm701Core
import zoo.ibm704.Ibm704Core
import zoo.ibm650.Ibm650Core
import zoo.ibm705.Ibm705Core
import zoo.ibm1401.Ibm1401Core
import zoo.stczebra.StczebraCore
import zoo.bullgamma60.Bullgamma60Core
import zoo.ibmstretch.IbmstretchCore
import zoo.univac1103a.Univac1103aCore
import zoo.cdc6600ppu.Cdc6600ppuCore
import zoo.decvax.DecvaxCore
import zoo.intel8080a.Intel8080aCore
import zoo.motorola6800.Motorola6800Core
import zoo.ibm6150.Ibm6150Core
import zoo.mips1.Mips1Core
import zoo.arm1.Arm1Core
import zoo.berkeleyrisc.BerkeleyriscCore
import zoo.hp3000.Hp3000Core
import zoo.ethlilith.EthlilithCore
import zoo.ucsdp.UcsdpCore
import zoo.upd7720.Upd7720Core
import zoo.tms32010.Tms32010Core
import zoo.adsp2100.Adsp2100Core
import zoo.ibmmwave.IbmmwaveCore
import zoo.voodoo1.Voodoo1Core
import zoo.geforce256.Geforce256Core
import zoo.radeonr100.Radeonr100Core
import zoo.powervr1.Powervr1Core
import zoo.mali200.Mali200Core
import zoo.amdr600.Amdr600Core
import zoo.amd2901.Amd2901Core
import zoo.intel3002.Intel3002Core
import zoo.imp16.Imp16Core
import zoo.mc10800.Mc10800Core
import zoo.illiac4.{Core => Illiac4Core}
import zoo.icldap.{Core => IcldapCore}
import zoo.goodmpp.{Core => GoodmppCore}
import zoo.cm1.{Core => Cm1Core}
import zoo.ibmmfast.{Core => IbmmfastCore}
import zoo.multiflow.{Core => MultiflowCore}
import zoo.cydra5.{Core => Cydra5Core}
import zoo.tms320c6k.{Core => Tms320c6kCore}
import zoo.crusoe.{Core => CrusoeCore}
import zoo.itanium.{Core => ItaniumCore}
import zoo.cdcstar100.{Core => CdcStar100Core}
import zoo.tiasc.{Core => TiAscCore}
import zoo.convexc1.{Core => ConvexC1Core}
import zoo.necsx2.{Core => NecSx2Core}
import zoo.ibms370vf.{Core => IbmS370VfCore}
import zoo.ibm801.Ibm801Core
import zoo.sparc.SparcCore
import zoo.powerpc.PowerpcCore
import zoo.jvm.JvmCore
import zoo.setun.SetunCore
import zoo.ibm1620.Ibm1620Core
import zoo.symbolics3600.Symbolics3600Core
import zoo.mitdataflow.MitDataflowCore
import zoo.subleq.SubleqCore
import zoo.soar.SoarCore
import zoo.tta.TtaCore

class ProfilerSpec extends AnyFlatSpec with Matchers {
  behavior of "ZooArchitectureProfiler"

  def findWorkspaceFile(relativePath: String): File = {
    var dir = new File(System.getProperty("user.dir"))
    var foundFile = new File(dir, relativePath)
    while (dir != null && !foundFile.exists()) {
      dir = dir.getParentFile
      if (dir != null) {
        foundFile = new File(dir, relativePath)
      }
    }
    if (foundFile.exists()) foundFile else new File(relativePath)
  }

  it should "profile and compare execution statistics for all 75 cores" in {
    println("\n=== RUNNING BENCHMARKS & GATHERING PMU STATS ===")

    // 1. DEC PDP-8
    val pdp8Hex = findWorkspaceFile("decpdp8/sw/test_add.hex")
    val pdp8Bytes = Source.fromFile(pdp8Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var pdp8Cycles = 0L
    var pdp8Insts = 0L
    var pdp8Reads = 0L
    var pdp8Writes = 0L

    simulate(new Pdp8Core) { c =>
      val mem = Array.fill(4096)(0)
      for (i <- pdp8Bytes.indices) mem(i) = pdp8Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      pdp8Cycles = c.io.pmu_cycles.peek().litValue.toLong
      pdp8Insts  = c.io.pmu_insts.peek().litValue.toLong
      pdp8Reads  = c.io.pmu_reads.peek().litValue.toLong
      pdp8Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(22) shouldBe 11
      mem(23) shouldBe 22
      mem(24) shouldBe 33
      mem(25) shouldBe 44
    }

    // 2. DEC PDP-11
    val pdp11Hex = findWorkspaceFile("decpdp11/sw/test_vector.hex")
    val pdp11Bytes = Source.fromFile(pdp11Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var pdp11Cycles = 0L
    var pdp11Insts = 0L
    var pdp11Reads = 0L
    var pdp11Writes = 0L

    simulate(new Pdp11Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- pdp11Bytes.indices) mem(i) = pdp11Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      pdp11Cycles = c.io.pmu_cycles.peek().litValue.toLong
      pdp11Insts  = c.io.pmu_insts.peek().litValue.toLong
      pdp11Reads  = c.io.pmu_reads.peek().litValue.toLong
      pdp11Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(28) shouldBe 11
      mem(29) shouldBe 22
      mem(30) shouldBe 33
      mem(31) shouldBe 44
    }

    // 3. MOS 6502
    val mosHex = findWorkspaceFile("mos6502/sw/test_vector.hex")
    val mosBytes = Source.fromFile(mosHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var mosCycles = 0L
    var mosInsts = 0L
    var mosReads = 0L
    var mosWrites = 0L

    simulate(new Mos6502Core) { c =>
      val mem = Array.fill(65536)(0)
      for (i <- mosBytes.indices) mem(i) = mosBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      mosCycles = c.io.pmu_cycles.peek().litValue.toLong
      mosInsts  = c.io.pmu_insts.peek().litValue.toLong
      mosReads  = c.io.pmu_reads.peek().litValue.toLong
      mosWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(68) shouldBe 11
      mem(69) shouldBe 22
      mem(70) shouldBe 33
      mem(71) shouldBe 44
    }

    // 4. IBM System/360
    val ibmHex = findWorkspaceFile("ibm360/sw/test_add.hex")
    val ibmBytes = Source.fromFile(ibmHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16).toByte).toArray
    var ibmCycles = 0L
    var ibmInsts = 0L
    var ibmReads = 0L
    var ibmWrites = 0L

    simulate(new Ibm360Core) { c =>
      val mem = Array.fill(1024)(0.toByte)
      for (i <- ibmBytes.indices) mem(i) = ibmBytes(i)

      def readWord(addr: Int): Int = {
        if (addr + 3 >= 1024) return 0
        ((mem(addr) & 0xFF) << 24) |
        ((mem(addr + 1) & 0xFF) << 16) |
        ((mem(addr + 2) & 0xFF) << 8) |
        (mem(addr + 3) & 0xFF)
      }

      def writeWord(addr: Int, data: Int): Unit = {
        if (addr + 3 >= 1024) return
        mem(addr)     = ((data >> 24) & 0xFF).toByte
        mem(addr + 1) = ((data >> 16) & 0xFF).toByte
        mem(addr + 2) = ((data >> 8) & 0xFF).toByte
        mem(addr + 3) = (data & 0xFF).toByte
      }

      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var done = false
      while (limit > 0 && !done) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) writeWord(addr, wdata)
          val rdata = readWord(addr)
          c.io.mem.rdata.poke((rdata.toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        val pcVal = c.io.pc_debug.peek().litValue.toInt
        if (pcVal == 48) done = true
      }
      ibmCycles = c.io.pmu_cycles.peek().litValue.toLong
      ibmInsts  = c.io.pmu_insts.peek().litValue.toLong
      ibmReads  = c.io.pmu_reads.peek().litValue.toLong
      ibmWrites = c.io.pmu_writes.peek().litValue.toLong

      readWord(84) shouldBe 11
      readWord(88) shouldBe 22
      readWord(92) shouldBe 33
      readWord(96) shouldBe 44
    }

    // 5. Motorola 68000
    val m68kHex = findWorkspaceFile("motorola68000/sw/test_add.hex")
    val m68kBytes = Source.fromFile(m68kHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var m68kCycles = 0L
    var m68kInsts = 0L
    var m68kReads = 0L
    var m68kWrites = 0L

    simulate(new M68kCore) { c =>
      val mem = scala.collection.mutable.Map[Long, Int]()
      for ((word, index) <- m68kBytes.zipWithIndex) {
        mem(index * 2L) = word
      }
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var done = false
      while (limit > 0 && !done) {
        val req = c.io.mem.req.peek().litValue > 0
        val addr = c.io.mem.addr.peek().litValue.toLong
        val write = c.io.mem.write.peek().litValue > 0
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem.getOrElse(addr, 0).U(16.W))
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        
        val pc = c.io.debug_pc.peek().litValue
        if (pc == 104) {
          done = true
        }
      }
      m68kCycles = c.io.pmu_cycles.peek().litValue.toLong
      m68kInsts  = c.io.pmu_insts.peek().litValue.toLong
      m68kReads  = c.io.pmu_reads.peek().litValue.toLong
      m68kWrites = c.io.pmu_writes.peek().litValue.toLong

      def read32(addr: Long): Int = {
        val high = mem.getOrElse(addr, 0)
        val low = mem.getOrElse(addr + 2, 0)
        (high << 16) | (low & 0xFFFF)
      }
      read32(168) shouldBe 11
      read32(172) shouldBe 22
      read32(176) shouldBe 33
      read32(180) shouldBe 44
    }

    // 6. Burroughs B5500
    val b5500Hex = findWorkspaceFile("burroughsb5500/sw/test_vector.hex")
    val b5500Bytes = Source.fromFile(b5500Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var b5500Cycles = 0L
    var b5500Insts = 0L
    var b5500Reads = 0L
    var b5500Writes = 0L

    simulate(new B5500Core) { c =>
      val mem = Array.fill(256)(0L)
      for (i <- b5500Bytes.indices) mem(i) = b5500Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      b5500Cycles = c.io.pmu_cycles.peek().litValue.toLong
      b5500Insts  = c.io.pmu_insts.peek().litValue.toLong
      b5500Reads  = c.io.pmu_reads.peek().litValue.toLong
      b5500Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(28) shouldBe 11L
      mem(29) shouldBe 22L
      mem(30) shouldBe 33L
      mem(31) shouldBe 44L
    }

    // 7. CDC 6600
    val cdcHex = findWorkspaceFile("cdc6600/sw/test_vector.hex")
    val cdcBytes = Source.fromFile(cdcHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var cdcCycles = 0L
    var cdcInsts = 0L
    var cdcReads = 0L
    var cdcWrites = 0L

    simulate(new Cdc6600Core) { c =>
      val mem = Array.fill(256)(0L)
      for (i <- cdcBytes.indices) mem(i) = cdcBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      cdcCycles = c.io.pmu_cycles.peek().litValue.toLong
      cdcInsts  = c.io.pmu_insts.peek().litValue.toLong
      cdcReads  = c.io.pmu_reads.peek().litValue.toLong
      cdcWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(28) shouldBe 11L
      mem(29) shouldBe 22L
      mem(30) shouldBe 33L
      mem(31) shouldBe 44L
    }

    // 8. Cray-1
    val crayHex = findWorkspaceFile("cray1/sw/test_vector.hex")
    val crayBytes = Source.fromFile(crayHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => BigInt(l.trim, 16)).toArray
    var crayCycles = 0L
    var crayInsts = 0L
    var crayReads = 0L
    var crayWrites = 0L

    simulate(new Cray1Core) { c =>
      val mem = Array.fill(1024)(BigInt(0))
      for (i <- crayBytes.indices) mem(i) = crayBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      crayCycles = c.io.pmu_cycles.peek().litValue.toLong
      crayInsts  = c.io.pmu_insts.peek().litValue.toLong
      crayReads  = c.io.pmu_reads.peek().litValue.toLong
      crayWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(24) shouldBe 11L
      mem(25) shouldBe 22L
      mem(26) shouldBe 33L
      mem(27) shouldBe 44L
    }

    // 9. Babbage Analytical Engine
    val babbageHex = findWorkspaceFile("babbage/sw/test_vector.hex")
    val babbageBytes = Source.fromFile(babbageHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var babbageCycles = 0L
    var babbageInsts = 0L
    var babbageReads = 0L
    var babbageWrites = 0L

    simulate(new BabbageCore) { c =>
      val mem = Array.fill(256)(0L)
      for (i <- babbageBytes.indices) mem(i) = babbageBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      babbageCycles = c.io.pmu_cycles.peek().litValue.toLong
      babbageInsts  = c.io.pmu_insts.peek().litValue.toLong
      babbageReads  = c.io.pmu_reads.peek().litValue.toLong
      babbageWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(28) shouldBe 11L
      mem(29) shouldBe 22L
      mem(30) shouldBe 33L
      mem(31) shouldBe 44L
    }

    // 10. Harvard Mark I
    val harvardHex = findWorkspaceFile("harvardmark1/sw/test_vector.hex")
    val harvardBytes = Source.fromFile(harvardHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var harvardCycles = 0L
    var harvardInsts = 0L
    var harvardReads = 0L
    var harvardWrites = 0L

    simulate(new HarvardMark1Core) { c =>
      val mem = Array.fill(256)(0L)
      for (i <- harvardBytes.indices) mem(i) = harvardBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.io.test_wen.poke(false.B)
      c.io.test_waddr.poke(0.U)
      c.io.test_wdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      // Pre-load inputs
      val vecA = Array(10L, 20L, 30L, 40L)
      val vecB = Array(1L, 2L, 3L, 4L)
      c.io.test_wen.poke(true.B)
      for (i <- 0 until 4) {
        c.io.test_waddr.poke((20 + i).U)
        c.io.test_wdata.poke(vecA(i).U)
        c.clock.step(1)
        c.io.test_waddr.poke((24 + i).U)
        c.io.test_wdata.poke(vecB(i).U)
        c.clock.step(1)
      }
      c.io.test_wen.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      harvardCycles = c.io.pmu_cycles.peek().litValue.toLong
      harvardInsts  = c.io.pmu_insts.peek().litValue.toLong
      harvardReads  = c.io.pmu_reads.peek().litValue.toLong
      harvardWrites = c.io.pmu_writes.peek().litValue.toLong

      c.io.regs_debug(28).peek().litValue shouldBe 11
      c.io.regs_debug(29).peek().litValue shouldBe 22
      c.io.regs_debug(30).peek().litValue shouldBe 33
      c.io.regs_debug(31).peek().litValue shouldBe 44
    }

    // 11. Zuse Z1
    val zuseHex = findWorkspaceFile("zusez1/sw/test_vector.hex")
    val zuseBytes = Source.fromFile(zuseHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var zuseCycles = 0L
    var zuseInsts = 0L
    var zuseReads = 0L
    var zuseWrites = 0L

    simulate(new ZuseZ1Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- zuseBytes.indices) mem(i) = zuseBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      zuseCycles = c.io.pmu_cycles.peek().litValue.toLong
      zuseInsts  = c.io.pmu_insts.peek().litValue.toLong
      zuseReads  = c.io.pmu_reads.peek().litValue.toLong
      zuseWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 12. Manchester Baby
    val manchestermu1Hex = findWorkspaceFile("manchestermu1/sw/test_vector.hex")
    val manchestermu1Bytes = Source.fromFile(manchestermu1Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseLong(l.trim, 16).toInt).toArray
    var manchestermu1Cycles = 0L
    var manchestermu1Insts = 0L
    var manchestermu1Reads = 0L
    var manchestermu1Writes = 0L

    simulate(new Manchestermu1Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- manchestermu1Bytes.indices) mem(i) = manchestermu1Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 1000
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      manchestermu1Cycles = c.io.pmu_cycles.peek().litValue.toLong
      manchestermu1Insts  = c.io.pmu_insts.peek().litValue.toLong
      manchestermu1Reads  = c.io.pmu_reads.peek().litValue.toLong
      manchestermu1Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 13. Univac I
    val univacHex = findWorkspaceFile("univac1/sw/test_vector.hex")
    val univacBytes = Source.fromFile(univacHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => BigInt(l.trim, 16)).toArray
    var univacCycles = 0L
    var univacInsts = 0L
    var univacReads = 0L
    var univacWrites = 0L

    simulate(new Univac1Core) { c =>
      val mem = Array.fill(256)(BigInt(0))
      for (i <- univacBytes.indices) mem(i) = univacBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      univacCycles = c.io.pmu_cycles.peek().litValue.toLong
      univacInsts  = c.io.pmu_insts.peek().litValue.toLong
      univacReads  = c.io.pmu_reads.peek().litValue.toLong
      univacWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe BigInt(11)
      mem(49) shouldBe BigInt(22)
      mem(50) shouldBe BigInt(33)
      mem(51) shouldBe BigInt(44)
    }

    // 14. Princeton IAS
    val princetoniasHex = findWorkspaceFile("princetonias/sw/test_vector.hex")
    val princetoniasBytes = Source.fromFile(princetoniasHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var princetoniasCycles = 0L
    var princetoniasInsts = 0L
    var princetoniasReads = 0L
    var princetoniasWrites = 0L

    simulate(new PrincetoniasCore) { c =>
      val mem = Array.fill(4096)(0L)
      for (i <- princetoniasBytes.indices) mem(i) = princetoniasBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      princetoniasCycles = c.io.pmu_cycles.peek().litValue.toLong
      princetoniasInsts  = c.io.pmu_insts.peek().litValue.toLong
      princetoniasReads  = c.io.pmu_reads.peek().litValue.toLong
      princetoniasWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11L
      mem(49) shouldBe 22L
      mem(50) shouldBe 33L
      mem(51) shouldBe 44L
    }

    // 15. EDSAC
    val cambridgeedsacHex = findWorkspaceFile("cambridgeedsac/sw/test_vector.hex")
    val cambridgeedsacBytes = Source.fromFile(cambridgeedsacHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var cambridgeedsacCycles = 0L
    var cambridgeedsacInsts = 0L
    var cambridgeedsacReads = 0L
    var cambridgeedsacWrites = 0L

    simulate(new CambridgeedsacCore) { c =>
      val mem = Array.fill(1024)(0)
      for (i <- cambridgeedsacBytes.indices) mem(i) = cambridgeedsacBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      cambridgeedsacCycles = c.io.pmu_cycles.peek().litValue.toLong
      cambridgeedsacInsts  = c.io.pmu_insts.peek().litValue.toLong
      cambridgeedsacReads  = c.io.pmu_reads.peek().litValue.toLong
      cambridgeedsacWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 16. IBM 701
    val ibm701Hex = findWorkspaceFile("ibm701/sw/test_vector.hex")
    val ibm701Bytes = Source.fromFile(ibm701Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var ibm701Cycles = 0L
    var ibm701Insts = 0L
    var ibm701Reads = 0L
    var ibm701Writes = 0L

    simulate(new Ibm701Core) { c =>
      val mem = Array.fill(4096)(0L)
      for (i <- ibm701Bytes.indices) mem(i) = ibm701Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      ibm701Cycles = c.io.pmu_cycles.peek().litValue.toLong
      ibm701Insts  = c.io.pmu_insts.peek().litValue.toLong
      ibm701Reads  = c.io.pmu_reads.peek().litValue.toLong
      ibm701Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11L
      mem(49) shouldBe 22L
      mem(50) shouldBe 33L
      mem(51) shouldBe 44L
    }

    // 17. IBM 704
    val ibm704Hex = findWorkspaceFile("ibm704/sw/test_vector.hex")
    val ibm704Bytes = Source.fromFile(ibm704Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var ibm704Cycles = 0L
    var ibm704Insts = 0L
    var ibm704Reads = 0L
    var ibm704Writes = 0L

    simulate(new Ibm704Core) { c =>
      val mem = Array.fill(32768)(0L)
      for (i <- ibm704Bytes.indices) mem(i) = ibm704Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      ibm704Cycles = c.io.pmu_cycles.peek().litValue.toLong
      ibm704Insts  = c.io.pmu_insts.peek().litValue.toLong
      ibm704Reads  = c.io.pmu_reads.peek().litValue.toLong
      ibm704Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11L
      mem(49) shouldBe 22L
      mem(50) shouldBe 33L
      mem(51) shouldBe 44L
    }

    // 18. IBM 650
    val ibm650Hex = findWorkspaceFile("ibm650/sw/test_vector.hex")
    val ibm650Bytes = Source.fromFile(ibm650Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var ibm650Cycles = 0L
    var ibm650Insts = 0L
    var ibm650Reads = 0L
    var ibm650Writes = 0L

    simulate(new Ibm650Core) { c =>
      val mem = Array.fill(4096)(0L)
      for (i <- ibm650Bytes.indices) mem(i) = ibm650Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      ibm650Cycles = c.io.pmu_cycles.peek().litValue.toLong
      ibm650Insts  = c.io.pmu_insts.peek().litValue.toLong
      ibm650Reads  = c.io.pmu_reads.peek().litValue.toLong
      ibm650Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11L
      mem(49) shouldBe 22L
      mem(50) shouldBe 33L
      mem(51) shouldBe 44L
    }

    // 19. IBM 705
    val ibm705Hex = findWorkspaceFile("ibm705/sw/test_vector.hex")
    val ibm705Bytes = Source.fromFile(ibm705Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var ibm705Cycles = 0L
    var ibm705Insts = 0L
    var ibm705Reads = 0L
    var ibm705Writes = 0L

    simulate(new Ibm705Core) { c =>
      val mem = Array.fill(4096)(0L)
      for (i <- ibm705Bytes.indices) mem(i) = ibm705Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      ibm705Cycles = c.io.pmu_cycles.peek().litValue.toLong
      ibm705Insts  = c.io.pmu_insts.peek().litValue.toLong
      ibm705Reads  = c.io.pmu_reads.peek().litValue.toLong
      ibm705Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11L
      mem(49) shouldBe 22L
      mem(50) shouldBe 33L
      mem(51) shouldBe 44L
    }

    // 20. IBM 1401
    val ibm1401Hex = findWorkspaceFile("ibm1401/sw/test_vector.hex")
    val ibm1401Bytes = Source.fromFile(ibm1401Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var ibm1401Cycles = 0L
    var ibm1401Insts = 0L
    var ibm1401Reads = 0L
    var ibm1401Writes = 0L

    simulate(new Ibm1401Core) { c =>
      val mem = Array.fill(4096)(0L)
      for (i <- ibm1401Bytes.indices) mem(i) = ibm1401Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      ibm1401Cycles = c.io.pmu_cycles.peek().litValue.toLong
      ibm1401Insts  = c.io.pmu_insts.peek().litValue.toLong
      ibm1401Reads  = c.io.pmu_reads.peek().litValue.toLong
      ibm1401Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11L
      mem(49) shouldBe 22L
      mem(50) shouldBe 33L
      mem(51) shouldBe 44L
    }

    // 21. STC ZEBRA
    val stczebraHex = findWorkspaceFile("stczebra/sw/test_vector.hex")
    val stczebraBytes = Source.fromFile(stczebraHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var stczebraCycles = 0L
    var stczebraInsts = 0L
    var stczebraReads = 0L
    var stczebraWrites = 0L

    simulate(new StczebraCore) { c =>
      val mem = Array.fill(256)(0L)
      for (i <- stczebraBytes.indices) mem(i) = stczebraBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      stczebraCycles = c.io.pmu_cycles.peek().litValue.toLong
      stczebraInsts  = c.io.pmu_insts.peek().litValue.toLong
      stczebraReads  = c.io.pmu_reads.peek().litValue.toLong
      stczebraWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11L
      mem(49) shouldBe 22L
      mem(50) shouldBe 33L
      mem(51) shouldBe 44L
    }

    // 22. Bull Gamma 60
    val bullgammaHex = findWorkspaceFile("bullgamma60/sw/test_vector.hex")
    val bullgammaBytes = Source.fromFile(bullgammaHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var bullgammaCycles = 0L
    var bullgammaInsts = 0L
    var bullgammaReads = 0L
    var bullgammaWrites = 0L

    simulate(new Bullgamma60Core) { c =>
      val mem = Array.fill(256)(0L)
      for (i <- bullgammaBytes.indices) mem(i) = bullgammaBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 1000
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      bullgammaCycles = c.io.pmu_cycles.peek().litValue.toLong
      bullgammaInsts  = c.io.pmu_insts.peek().litValue.toLong
      bullgammaReads  = c.io.pmu_reads.peek().litValue.toLong
      bullgammaWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11L
      mem(49) shouldBe 22L
      mem(50) shouldBe 33L
      mem(51) shouldBe 44L
    }

    // 23. IBM Stretch
    val ibmstretchHex = findWorkspaceFile("ibmstretch/sw/test_vector.hex")
    val ibmstretchBytes = Source.fromFile(ibmstretchHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var ibmstretchCycles = 0L
    var ibmstretchInsts = 0L
    var ibmstretchReads = 0L
    var ibmstretchWrites = 0L

    simulate(new IbmstretchCore) { c =>
      val mem = Array.fill(256)(0L)
      for (i <- ibmstretchBytes.indices) mem(i) = ibmstretchBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 1000
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      ibmstretchCycles = c.io.pmu_cycles.peek().litValue.toLong
      ibmstretchInsts  = c.io.pmu_insts.peek().litValue.toLong
      ibmstretchReads  = c.io.pmu_reads.peek().litValue.toLong
      ibmstretchWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11L
      mem(49) shouldBe 22L
      mem(50) shouldBe 33L
      mem(51) shouldBe 44L
    }

    // 24. UNIVAC 1103A
    val univac1103aHex = findWorkspaceFile("univac1103a/sw/test_vector.hex")
    val univac1103aBytes = Source.fromFile(univac1103aHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var univac1103aCycles = 0L
    var univac1103aInsts = 0L
    var univac1103aReads = 0L
    var univac1103aWrites = 0L

    simulate(new Univac1103aCore) { c =>
      val mem = Array.fill(256)(0L)
      for (i <- univac1103aBytes.indices) mem(i) = univac1103aBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      univac1103aCycles = c.io.pmu_cycles.peek().litValue.toLong
      univac1103aInsts  = c.io.pmu_insts.peek().litValue.toLong
      univac1103aReads  = c.io.pmu_reads.peek().litValue.toLong
      univac1103aWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11L
      mem(49) shouldBe 22L
      mem(50) shouldBe 33L
      mem(51) shouldBe 44L
    }

    // 25. CDC 6600 PPU
    val cdc6600ppuHex = findWorkspaceFile("cdc6600ppu/sw/test_vector.hex")
    val cdc6600ppuBytes = Source.fromFile(cdc6600ppuHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var cdc6600ppuCycles = 0L
    var cdc6600ppuInsts = 0L
    var cdc6600ppuReads = 0L
    var cdc6600ppuWrites = 0L

    simulate(new Cdc6600ppuCore) { c =>
      val mem = Array.fill(256)(0L)
      for (i <- cdc6600ppuBytes.indices) mem(i) = cdc6600ppuBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      cdc6600ppuCycles = c.io.pmu_cycles.peek().litValue.toLong
      cdc6600ppuInsts  = c.io.pmu_insts.peek().litValue.toLong
      cdc6600ppuReads  = c.io.pmu_reads.peek().litValue.toLong
      cdc6600ppuWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11L
      mem(49) shouldBe 22L
      mem(50) shouldBe 33L
      mem(51) shouldBe 44L
    }

    // 26. DEC VAX
    val decvaxHex = findWorkspaceFile("decvax/sw/test_vector.hex")
    val decvaxBytes = Source.fromFile(decvaxHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var decvaxCycles = 0L
    var decvaxInsts = 0L
    var decvaxReads = 0L
    var decvaxWrites = 0L

    simulate(new DecvaxCore) { c =>
      val mem = Array.fill(256)(0L)
      for (i <- decvaxBytes.indices) mem(i) = decvaxBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      decvaxCycles = c.io.pmu_cycles.peek().litValue.toLong
      decvaxInsts  = c.io.pmu_insts.peek().litValue.toLong
      decvaxReads  = c.io.pmu_reads.peek().litValue.toLong
      decvaxWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(28) shouldBe 11L
      mem(29) shouldBe 22L
      mem(30) shouldBe 33L
      mem(31) shouldBe 44L
    }

    // 27. Intel 8080A
    val intel8080aHex = findWorkspaceFile("intel8080a/sw/test_vector.hex")
    val intel8080aBytes = Source.fromFile(intel8080aHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var intel8080aCycles = 0L
    var intel8080aInsts = 0L
    var intel8080aReads = 0L
    var intel8080aWrites = 0L

    simulate(new Intel8080aCore) { c =>
      val mem = Array.fill(65536)(0)
      for (i <- intel8080aBytes.indices) mem(i) = intel8080aBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      intel8080aCycles = c.io.pmu_cycles.peek().litValue.toLong
      intel8080aInsts  = c.io.pmu_insts.peek().litValue.toLong
      intel8080aReads  = c.io.pmu_reads.peek().litValue.toLong
      intel8080aWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(88) shouldBe 11
      mem(89) shouldBe 22
      mem(90) shouldBe 33
      mem(91) shouldBe 44
    }

    // 28. Motorola 6800
    val motorola6800Hex = findWorkspaceFile("motorola6800/sw/test_vector.hex")
    val motorola6800Bytes = Source.fromFile(motorola6800Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var motorola6800Cycles = 0L
    var motorola6800Insts = 0L
    var motorola6800Reads = 0L
    var motorola6800Writes = 0L

    simulate(new Motorola6800Core) { c =>
      val mem = Array.fill(65536)(0)
      for (i <- motorola6800Bytes.indices) mem(i) = motorola6800Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      motorola6800Cycles = c.io.pmu_cycles.peek().litValue.toLong
      motorola6800Insts  = c.io.pmu_insts.peek().litValue.toLong
      motorola6800Reads  = c.io.pmu_reads.peek().litValue.toLong
      motorola6800Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(88) shouldBe 11
      mem(89) shouldBe 22
      mem(90) shouldBe 33
      mem(91) shouldBe 44
    }

    // 29. IBM 6150 (ROMP)
    val ibm6150Hex = findWorkspaceFile("ibm6150/sw/test_vector.hex")
    val ibm6150Bytes = Source.fromFile(ibm6150Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var ibm6150Cycles = 0L
    var ibm6150Insts = 0L
    var ibm6150Reads = 0L
    var ibm6150Writes = 0L

    simulate(new Ibm6150Core) { c =>
      val mem = Array.fill(256)(0L)
      for (i <- ibm6150Bytes.indices) mem(i) = ibm6150Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      ibm6150Cycles = c.io.pmu_cycles.peek().litValue.toLong
      ibm6150Insts  = c.io.pmu_insts.peek().litValue.toLong
      ibm6150Reads  = c.io.pmu_reads.peek().litValue.toLong
      ibm6150Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11L
      mem(49) shouldBe 22L
      mem(50) shouldBe 33L
      mem(51) shouldBe 44L
    }

    // 30. MIPS I (R2000)
    val mips1Hex = findWorkspaceFile("mips1/sw/test_vector.hex")
    val mips1Bytes = Source.fromFile(mips1Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var mips1Cycles = 0L
    var mips1Insts = 0L
    var mips1Reads = 0L
    var mips1Writes = 0L

    simulate(new Mips1Core) { c =>
      val mem = Array.fill(256)(0L)
      for (i <- mips1Bytes.indices) mem(i) = mips1Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      mips1Cycles = c.io.pmu_cycles.peek().litValue.toLong
      mips1Insts  = c.io.pmu_insts.peek().litValue.toLong
      mips1Reads  = c.io.pmu_reads.peek().litValue.toLong
      mips1Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11L
      mem(49) shouldBe 22L
      mem(50) shouldBe 33L
      mem(51) shouldBe 44L
    }

    // 31. ARM1
    val arm1Hex = findWorkspaceFile("arm1/sw/test_vector.hex")
    val arm1Bytes = Source.fromFile(arm1Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var arm1Cycles = 0L
    var arm1Insts = 0L
    var arm1Reads = 0L
    var arm1Writes = 0L

    simulate(new Arm1Core) { c =>
      val mem = Array.fill(256)(0L)
      for (i <- arm1Bytes.indices) mem(i) = arm1Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      arm1Cycles = c.io.pmu_cycles.peek().litValue.toLong
      arm1Insts  = c.io.pmu_insts.peek().litValue.toLong
      arm1Reads  = c.io.pmu_reads.peek().litValue.toLong
      arm1Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11L
      mem(49) shouldBe 22L
      mem(50) shouldBe 33L
      mem(51) shouldBe 44L
    }

    // 32. Berkeley RISC-I
    val berkeleyriscHex = findWorkspaceFile("berkeleyrisc/sw/test_vector.hex")
    val berkeleyriscBytes = Source.fromFile(berkeleyriscHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var berkeleyriscCycles = 0L
    var berkeleyriscInsts = 0L
    var berkeleyriscReads = 0L
    var berkeleyriscWrites = 0L

    simulate(new BerkeleyriscCore) { c =>
      val mem = Array.fill(256)(0L)
      for (i <- berkeleyriscBytes.indices) mem(i) = berkeleyriscBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      berkeleyriscCycles = c.io.pmu_cycles.peek().litValue.toLong
      berkeleyriscInsts  = c.io.pmu_insts.peek().litValue.toLong
      berkeleyriscReads  = c.io.pmu_reads.peek().litValue.toLong
      berkeleyriscWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11L
      mem(49) shouldBe 22L
      mem(50) shouldBe 33L
      mem(51) shouldBe 44L
    }

    // 33. HP 3000
    val hp3000Hex = findWorkspaceFile("hp3000/sw/test_vector.hex")
    val hp3000Bytes = Source.fromFile(hp3000Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var hp3000Cycles = 0L
    var hp3000Insts = 0L
    var hp3000Reads = 0L
    var hp3000Writes = 0L

    simulate(new Hp3000Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- hp3000Bytes.indices) mem(i) = hp3000Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      hp3000Cycles = c.io.pmu_cycles.peek().litValue.toLong
      hp3000Insts  = c.io.pmu_insts.peek().litValue.toLong
      hp3000Reads  = c.io.pmu_reads.peek().litValue.toLong
      hp3000Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 34. Ethlilith
    val ethlilithHex = findWorkspaceFile("ethlilith/sw/test_vector.hex")
    val ethlilithBytes = Source.fromFile(ethlilithHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var ethlilithCycles = 0L
    var ethlilithInsts = 0L
    var ethlilithReads = 0L
    var ethlilithWrites = 0L

    simulate(new EthlilithCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- ethlilithBytes.indices) mem(i) = ethlilithBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      ethlilithCycles = c.io.pmu_cycles.peek().litValue.toLong
      ethlilithInsts  = c.io.pmu_insts.peek().litValue.toLong
      ethlilithReads  = c.io.pmu_reads.peek().litValue.toLong
      ethlilithWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 35. UCSD Pascal P-Machine
    val ucsdpHex = findWorkspaceFile("ucsdp/sw/test_vector.hex")
    val ucsdpBytes = Source.fromFile(ucsdpHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var ucsdpCycles = 0L
    var ucsdpInsts = 0L
    var ucsdpReads = 0L
    var ucsdpWrites = 0L

    simulate(new UcsdpCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- ucsdpBytes.indices) mem(i) = ucsdpBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      ucsdpCycles = c.io.pmu_cycles.peek().litValue.toLong
      ucsdpInsts  = c.io.pmu_insts.peek().litValue.toLong
      ucsdpReads  = c.io.pmu_reads.peek().litValue.toLong
      ucsdpWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 36. NEC uPD7720
    val upd7720Hex = findWorkspaceFile("upd7720/sw/test_vector.hex")
    val upd7720Bytes = Source.fromFile(upd7720Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var upd7720Cycles = 0L
    var upd7720Insts = 0L
    var upd7720Reads = 0L
    var upd7720Writes = 0L

    simulate(new Upd7720Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- upd7720Bytes.indices) mem(i) = upd7720Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      upd7720Cycles = c.io.pmu_cycles.peek().litValue.toLong
      upd7720Insts  = c.io.pmu_insts.peek().litValue.toLong
      upd7720Reads  = c.io.pmu_reads.peek().litValue.toLong
      upd7720Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 37. TI TMS32010
    val tms32010Hex = findWorkspaceFile("tms32010/sw/test_vector.hex")
    val tms32010Bytes = Source.fromFile(tms32010Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var tms32010Cycles = 0L
    var tms32010Insts = 0L
    var tms32010Reads = 0L
    var tms32010Writes = 0L

    simulate(new Tms32010Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- tms32010Bytes.indices) mem(i) = tms32010Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      tms32010Cycles = c.io.pmu_cycles.peek().litValue.toLong
      tms32010Insts  = c.io.pmu_insts.peek().litValue.toLong
      tms32010Reads  = c.io.pmu_reads.peek().litValue.toLong
      tms32010Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 38. ADI ADSP-2100
    val adsp2100Hex = findWorkspaceFile("adsp2100/sw/test_vector.hex")
    val adsp2100Bytes = Source.fromFile(adsp2100Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var adsp2100Cycles = 0L
    var adsp2100Insts = 0L
    var adsp2100Reads = 0L
    var adsp2100Writes = 0L

    simulate(new Adsp2100Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- adsp2100Bytes.indices) mem(i) = adsp2100Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      adsp2100Cycles = c.io.pmu_cycles.peek().litValue.toLong
      adsp2100Insts  = c.io.pmu_insts.peek().litValue.toLong
      adsp2100Reads  = c.io.pmu_reads.peek().litValue.toLong
      adsp2100Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 39. IBM MWave
    val ibmmwaveHex = findWorkspaceFile("ibmmwave/sw/test_vector.hex")
    val ibmmwaveBytes = Source.fromFile(ibmmwaveHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var ibmmwaveCycles = 0L
    var ibmmwaveInsts = 0L
    var ibmmwaveReads = 0L
    var ibmmwaveWrites = 0L

    simulate(new IbmmwaveCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- ibmmwaveBytes.indices) mem(i) = ibmmwaveBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      ibmmwaveCycles = c.io.pmu_cycles.peek().litValue.toLong
      ibmmwaveInsts  = c.io.pmu_insts.peek().litValue.toLong
      ibmmwaveReads  = c.io.pmu_reads.peek().litValue.toLong
      ibmmwaveWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 40. 3dfx Voodoo1
    val voodoo1Hex = findWorkspaceFile("voodoo1/sw/test_vector.hex")
    val voodoo1Bytes = Source.fromFile(voodoo1Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => {
      if (l.trim.length > 8) java.lang.Long.parseLong(l.trim.substring(l.trim.length - 8), 16).toInt
      else java.lang.Long.parseLong(l.trim, 16).toInt
    }).toArray
    var voodoo1Cycles = 0L
    var voodoo1Insts = 0L
    var voodoo1Reads = 0L
    var voodoo1Writes = 0L

    simulate(new Voodoo1Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- voodoo1Bytes.indices) mem(i) = voodoo1Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      voodoo1Cycles = c.io.pmu_cycles.peek().litValue.toLong
      voodoo1Insts  = c.io.pmu_insts.peek().litValue.toLong
      voodoo1Reads  = c.io.pmu_reads.peek().litValue.toLong
      voodoo1Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 41. NVIDIA GeForce 256
    val geforce256Hex = findWorkspaceFile("geforce256/sw/test_vector.hex")
    val geforce256Bytes = Source.fromFile(geforce256Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => {
      if (l.trim.length > 8) java.lang.Long.parseLong(l.trim.substring(l.trim.length - 8), 16).toInt
      else java.lang.Long.parseLong(l.trim, 16).toInt
    }).toArray
    var geforce256Cycles = 0L
    var geforce256Insts = 0L
    var geforce256Reads = 0L
    var geforce256Writes = 0L

    simulate(new Geforce256Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- geforce256Bytes.indices) mem(i) = geforce256Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      geforce256Cycles = c.io.pmu_cycles.peek().litValue.toLong
      geforce256Insts  = c.io.pmu_insts.peek().litValue.toLong
      geforce256Reads  = c.io.pmu_reads.peek().litValue.toLong
      geforce256Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 42. ATI Radeon R100
    val radeonr100Hex = findWorkspaceFile("radeonr100/sw/test_vector.hex")
    val radeonr100Bytes = Source.fromFile(radeonr100Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => {
      if (l.trim.length > 8) java.lang.Long.parseLong(l.trim.substring(l.trim.length - 8), 16).toInt
      else java.lang.Long.parseLong(l.trim, 16).toInt
    }).toArray
    var radeonr100Cycles = 0L
    var radeonr100Insts = 0L
    var radeonr100Reads = 0L
    var radeonr100Writes = 0L

    simulate(new Radeonr100Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- radeonr100Bytes.indices) mem(i) = radeonr100Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      radeonr100Cycles = c.io.pmu_cycles.peek().litValue.toLong
      radeonr100Insts  = c.io.pmu_insts.peek().litValue.toLong
      radeonr100Reads  = c.io.pmu_reads.peek().litValue.toLong
      radeonr100Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 43. PowerVR Series 1
    val powervr1Hex = findWorkspaceFile("powervr1/sw/test_vector.hex")
    val powervr1Bytes = Source.fromFile(powervr1Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => {
      if (l.trim.length > 8) java.lang.Long.parseLong(l.trim.substring(l.trim.length - 8), 16).toInt
      else java.lang.Long.parseLong(l.trim, 16).toInt
    }).toArray
    var powervr1Cycles = 0L
    var powervr1Insts = 0L
    var powervr1Reads = 0L
    var powervr1Writes = 0L

    simulate(new Powervr1Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- powervr1Bytes.indices) mem(i) = powervr1Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      powervr1Cycles = c.io.pmu_cycles.peek().litValue.toLong
      powervr1Insts  = c.io.pmu_insts.peek().litValue.toLong
      powervr1Reads  = c.io.pmu_reads.peek().litValue.toLong
      powervr1Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 44. ARM Mali-200
    val mali200Hex = findWorkspaceFile("mali200/sw/test_vector.hex")
    val mali200Bytes = Source.fromFile(mali200Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => {
      if (l.trim.length > 8) java.lang.Long.parseLong(l.trim.substring(l.trim.length - 8), 16).toInt
      else java.lang.Long.parseLong(l.trim, 16).toInt
    }).toArray
    var mali200Cycles = 0L
    var mali200Insts = 0L
    var mali200Reads = 0L
    var mali200Writes = 0L

    simulate(new Mali200Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- mali200Bytes.indices) mem(i) = mali200Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      mali200Cycles = c.io.pmu_cycles.peek().litValue.toLong
      mali200Insts  = c.io.pmu_insts.peek().litValue.toLong
      mali200Reads  = c.io.pmu_reads.peek().litValue.toLong
      mali200Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 45. AMD R600
    val amdr600Hex = findWorkspaceFile("amdr600/sw/test_vector.hex")
    val amdr600Bytes = Source.fromFile(amdr600Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => {
      if (l.trim.length > 8) java.lang.Long.parseLong(l.trim.substring(l.trim.length - 8), 16).toInt
      else java.lang.Long.parseLong(l.trim, 16).toInt
    }).toArray
    var amdr600Cycles = 0L
    var amdr600Insts = 0L
    var amdr600Reads = 0L
    var amdr600Writes = 0L

    simulate(new Amdr600Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- amdr600Bytes.indices) mem(i) = amdr600Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      amdr600Cycles = c.io.pmu_cycles.peek().litValue.toLong
      amdr600Insts  = c.io.pmu_insts.peek().litValue.toLong
      amdr600Reads  = c.io.pmu_reads.peek().litValue.toLong
      amdr600Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 46. AMD Am2901
    val amd2901Hex = findWorkspaceFile("amd2901/sw/test_vector.hex")
    val amd2901Bytes = Source.fromFile(amd2901Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var amd2901Cycles = 0L
    var amd2901Insts = 0L
    var amd2901Reads = 0L
    var amd2901Writes = 0L

    simulate(new Amd2901Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- amd2901Bytes.indices) mem(i) = amd2901Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      amd2901Cycles = c.io.pmu_cycles.peek().litValue.toLong
      amd2901Insts  = c.io.pmu_insts.peek().litValue.toLong
      amd2901Reads  = c.io.pmu_reads.peek().litValue.toLong
      amd2901Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(37) shouldBe 11
      mem(38) shouldBe 22
      mem(39) shouldBe 33
      mem(40) shouldBe 44
    }

    // 47. Intel 3002
    val intel3002Hex = findWorkspaceFile("intel3002/sw/test_vector.hex")
    val intel3002Bytes = Source.fromFile(intel3002Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var intel3002Cycles = 0L
    var intel3002Insts = 0L
    var intel3002Reads = 0L
    var intel3002Writes = 0L

    simulate(new Intel3002Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- intel3002Bytes.indices) mem(i) = intel3002Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      intel3002Cycles = c.io.pmu_cycles.peek().litValue.toLong
      intel3002Insts  = c.io.pmu_insts.peek().litValue.toLong
      intel3002Reads  = c.io.pmu_reads.peek().litValue.toLong
      intel3002Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(37) shouldBe 11
      mem(38) shouldBe 22
      mem(39) shouldBe 33
      mem(40) shouldBe 44
    }

    // 48. National Semiconductor IMP-16
    val imp16Hex = findWorkspaceFile("imp16/sw/test_vector.hex")
    val imp16Bytes = Source.fromFile(imp16Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var imp16Cycles = 0L
    var imp16Insts = 0L
    var imp16Reads = 0L
    var imp16Writes = 0L

    simulate(new Imp16Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- imp16Bytes.indices) mem(i) = imp16Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 1500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      imp16Cycles = c.io.pmu_cycles.peek().litValue.toLong
      imp16Insts  = c.io.pmu_insts.peek().litValue.toLong
      imp16Reads  = c.io.pmu_reads.peek().litValue.toLong
      imp16Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(65) shouldBe 11
      mem(66) shouldBe 22
      mem(67) shouldBe 33
      mem(68) shouldBe 44
    }

    // 49. Motorola MC10800
    val mc10800Hex = findWorkspaceFile("mc10800/sw/test_vector.hex")
    val mc10800Bytes = Source.fromFile(mc10800Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var mc10800Cycles = 0L
    var mc10800Insts = 0L
    var mc10800Reads = 0L
    var mc10800Writes = 0L

    simulate(new Mc10800Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- mc10800Bytes.indices) mem(i) = mc10800Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 1500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      mc10800Cycles = c.io.pmu_cycles.peek().litValue.toLong
      mc10800Insts  = c.io.pmu_insts.peek().litValue.toLong
      mc10800Reads  = c.io.pmu_reads.peek().litValue.toLong
      mc10800Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(65) shouldBe 11
      mem(66) shouldBe 22
      mem(67) shouldBe 33
      mem(68) shouldBe 44
    }

    // 50. ILLIAC IV
    val illiac4Hex = findWorkspaceFile("illiac4/sw/test_vector.hex")
    val illiac4Bytes = Source.fromFile(illiac4Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseLong(l.trim, 16)).toArray
    var illiac4Cycles = 0L
    var illiac4Insts = 0L
    var illiac4Reads = 0L
    var illiac4Writes = 0L

    simulate(new Illiac4Core) { c =>
      val mem = Array.fill(256)(0L)
      for (i <- illiac4Bytes.indices) mem(i) = illiac4Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 1500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      illiac4Cycles = c.io.pmu_cycles.peek().litValue.toLong
      illiac4Insts  = c.io.pmu_insts.peek().litValue.toLong
      illiac4Reads  = c.io.pmu_reads.peek().litValue.toLong
      illiac4Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(18) shouldBe 11
      mem(19) shouldBe 22
      mem(20) shouldBe 33
      mem(21) shouldBe 44
    }

    // 51. ICL DAP
    val icldapHex = findWorkspaceFile("icldap/sw/test_vector.hex")
    val icldapBytes = Source.fromFile(icldapHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var icldapCycles = 0L
    var icldapInsts = 0L
    var icldapReads = 0L
    var icldapWrites = 0L

    simulate(new IcldapCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- icldapBytes.indices) mem(i) = icldapBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 1500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      icldapCycles = c.io.pmu_cycles.peek().litValue.toLong
      icldapInsts  = c.io.pmu_insts.peek().litValue.toLong
      icldapReads  = c.io.pmu_reads.peek().litValue.toLong
      icldapWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(154) shouldBe 11
      mem(155) shouldBe 22
      mem(156) shouldBe 33
      mem(157) shouldBe 44
    }

    // 52. Goodyear MPP
    val goodmppHex = findWorkspaceFile("goodmpp/sw/test_vector.hex")
    val goodmppBytes = Source.fromFile(goodmppHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var goodmppCycles = 0L
    var goodmppInsts = 0L
    var goodmppReads = 0L
    var goodmppWrites = 0L

    simulate(new GoodmppCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- goodmppBytes.indices) mem(i) = goodmppBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 1500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      goodmppCycles = c.io.pmu_cycles.peek().litValue.toLong
      goodmppInsts  = c.io.pmu_insts.peek().litValue.toLong
      goodmppReads  = c.io.pmu_reads.peek().litValue.toLong
      goodmppWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(161) shouldBe 11
      mem(162) shouldBe 22
      mem(163) shouldBe 33
      mem(164) shouldBe 44
    }

    // 53. Connection Machine CM-1
    val cm1Hex = findWorkspaceFile("cm1/sw/test_vector.hex")
    val cm1Bytes = Source.fromFile(cm1Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var cm1Cycles = 0L
    var cm1Insts = 0L
    var cm1Reads = 0L
    var cm1Writes = 0L

    simulate(new Cm1Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- cm1Bytes.indices) mem(i) = cm1Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 1500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      cm1Cycles = c.io.pmu_cycles.peek().litValue.toLong
      cm1Insts  = c.io.pmu_insts.peek().litValue.toLong
      cm1Reads  = c.io.pmu_reads.peek().litValue.toLong
      cm1Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(154) shouldBe 11
      mem(155) shouldBe 22
      mem(156) shouldBe 33
      mem(157) shouldBe 44
    }

    // 54. IBM MFAST
    val ibmmfastHex = findWorkspaceFile("ibmmfast/sw/test_vector.hex")
    val ibmmfastBytes = Source.fromFile(ibmmfastHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var ibmmfastCycles = 0L
    var ibmmfastInsts = 0L
    var ibmmfastReads = 0L
    var ibmmfastWrites = 0L

    simulate(new IbmmfastCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- ibmmfastBytes.indices) mem(i) = ibmmfastBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      ibmmfastCycles = c.io.pmu_cycles.peek().litValue.toLong
      ibmmfastInsts  = c.io.pmu_insts.peek().litValue.toLong
      ibmmfastReads  = c.io.pmu_reads.peek().litValue.toLong
      ibmmfastWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(24) shouldBe 11
      mem(25) shouldBe 22
      mem(26) shouldBe 33
      mem(27) shouldBe 44
    }

    // 55. Multiflow TRACE
    val multiflowHex = findWorkspaceFile("multiflow/sw/test_vector.hex")
    val multiflowBytes = Source.fromFile(multiflowHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var multiflowCycles = 0L
    var multiflowInsts = 0L
    var multiflowReads = 0L
    var multiflowWrites = 0L

    simulate(new MultiflowCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- multiflowBytes.indices) mem(i) = multiflowBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      multiflowCycles = c.io.pmu_cycles.peek().litValue.toLong
      multiflowInsts  = c.io.pmu_insts.peek().litValue.toLong
      multiflowReads  = c.io.pmu_reads.peek().litValue.toLong
      multiflowWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(92) shouldBe 11
      mem(93) shouldBe 22
      mem(94) shouldBe 33
      mem(95) shouldBe 44
    }

    // 56. Cydrome Cydra 5
    val cydra5Hex = findWorkspaceFile("cydra5/sw/test_vector.hex")
    val cydra5Bytes = Source.fromFile(cydra5Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var cydra5Cycles = 0L
    var cydra5Insts = 0L
    var cydra5Reads = 0L
    var cydra5Writes = 0L

    simulate(new Cydra5Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- cydra5Bytes.indices) mem(i) = cydra5Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      cydra5Cycles = c.io.pmu_cycles.peek().litValue.toLong
      cydra5Insts  = c.io.pmu_insts.peek().litValue.toLong
      cydra5Reads  = c.io.pmu_reads.peek().litValue.toLong
      cydra5Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(92) shouldBe 11
      mem(93) shouldBe 22
      mem(94) shouldBe 33
      mem(95) shouldBe 44
    }

    // 57. TI TMS320C6000
    val tms320c6kHex = findWorkspaceFile("tms320c6k/sw/test_vector.hex")
    val tms320c6kBytes = Source.fromFile(tms320c6kHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var tms320c6kCycles = 0L
    var tms320c6kInsts = 0L
    var tms320c6kReads = 0L
    var tms320c6kWrites = 0L

    simulate(new Tms320c6kCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- tms320c6kBytes.indices) mem(i) = tms320c6kBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      tms320c6kCycles = c.io.pmu_cycles.peek().litValue.toLong
      tms320c6kInsts  = c.io.pmu_insts.peek().litValue.toLong
      tms320c6kReads  = c.io.pmu_reads.peek().litValue.toLong
      tms320c6kWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(92) shouldBe 11
      mem(93) shouldBe 22
      mem(94) shouldBe 33
      mem(95) shouldBe 44
    }

    // 58. Transmeta Crusoe
    val crusoeHex = findWorkspaceFile("crusoe/sw/test_vector.hex")
    val crusoeBytes = Source.fromFile(crusoeHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var crusoeCycles = 0L
    var crusoeInsts = 0L
    var crusoeReads = 0L
    var crusoeWrites = 0L

    simulate(new CrusoeCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- crusoeBytes.indices) mem(i) = crusoeBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      crusoeCycles = c.io.pmu_cycles.peek().litValue.toLong
      crusoeInsts  = c.io.pmu_insts.peek().litValue.toLong
      crusoeReads  = c.io.pmu_reads.peek().litValue.toLong
      crusoeWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(92) shouldBe 11
      mem(93) shouldBe 22
      mem(94) shouldBe 33
      mem(95) shouldBe 44
    }

    // 59. Intel Itanium
    val itaniumHex = findWorkspaceFile("itanium/sw/test_vector.hex")
    val itaniumBytes = Source.fromFile(itaniumHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16).toInt).toArray
    var itaniumCycles = 0L
    var itaniumInsts = 0L
    var itaniumReads = 0L
    var itaniumWrites = 0L

    simulate(new ItaniumCore) { c =>
      val mem = Array.fill(256)(0L)
      for (i <- 0 until (itaniumBytes.length / 2)) {
        val w0 = itaniumBytes(2 * i).toLong & 0xFFFFFFFFL
        val w1 = itaniumBytes(2 * i + 1).toLong & 0xFFFFFFFFL
        mem(i) = (w1 << 32) | w0
      }
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          val wordAddr = addr >> 1
          if (write) mem(wordAddr) = wdata
          c.io.mem.rdata.poke(mem(wordAddr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      itaniumCycles = c.io.pmu_cycles.peek().litValue.toLong
      itaniumInsts  = c.io.pmu_insts.peek().litValue.toLong
      itaniumReads  = c.io.pmu_reads.peek().litValue.toLong
      itaniumWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(50) shouldBe 11L
      mem(51) shouldBe 22L
      mem(52) shouldBe 33L
      mem(53) shouldBe 44L
    }

    // 60. CDC STAR-100
    val cdcstar100Hex = findWorkspaceFile("cdcstar100/sw/test_vector.hex")
    val cdcstar100Bytes = Source.fromFile(cdcstar100Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16).toInt).toArray
    var cdcstar100Cycles = 0L
    var cdcstar100Insts = 0L
    var cdcstar100Reads = 0L
    var cdcstar100Writes = 0L

    simulate(new CdcStar100Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- cdcstar100Bytes.indices) mem(i) = cdcstar100Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      cdcstar100Cycles = c.io.pmu_cycles.peek().litValue.toLong
      cdcstar100Insts  = c.io.pmu_insts.peek().litValue.toLong
      cdcstar100Reads  = c.io.pmu_reads.peek().litValue.toLong
      cdcstar100Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(92) shouldBe 11
      mem(93) shouldBe 22
      mem(94) shouldBe 33
      mem(95) shouldBe 44
    }

    // 61. TI ASC
    val tiascHex = findWorkspaceFile("tiasc/sw/test_vector.hex")
    val tiascBytes = Source.fromFile(tiascHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16).toInt).toArray
    var tiascCycles = 0L
    var tiascInsts = 0L
    var tiascReads = 0L
    var tiascWrites = 0L

    simulate(new TiAscCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- tiascBytes.indices) mem(i) = tiascBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      tiascCycles = c.io.pmu_cycles.peek().litValue.toLong
      tiascInsts  = c.io.pmu_insts.peek().litValue.toLong
      tiascReads  = c.io.pmu_reads.peek().litValue.toLong
      tiascWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(92) shouldBe 11
      mem(93) shouldBe 22
      mem(94) shouldBe 33
      mem(95) shouldBe 44
    }

    // 62. Convex C1
    val convexc1Hex = findWorkspaceFile("convexc1/sw/test_vector.hex")
    val convexc1Bytes = Source.fromFile(convexc1Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16).toInt).toArray
    var convexc1Cycles = 0L
    var convexc1Insts = 0L
    var convexc1Reads = 0L
    var convexc1Writes = 0L

    simulate(new ConvexC1Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- convexc1Bytes.indices) mem(i) = convexc1Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      convexc1Cycles = c.io.pmu_cycles.peek().litValue.toLong
      convexc1Insts  = c.io.pmu_insts.peek().litValue.toLong
      convexc1Reads  = c.io.pmu_reads.peek().litValue.toLong
      convexc1Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(92) shouldBe 11
      mem(93) shouldBe 22
      mem(94) shouldBe 33
      mem(95) shouldBe 44
    }

    // 63. NEC SX-2
    val necsx2Hex = findWorkspaceFile("necsx2/sw/test_vector.hex")
    val necsx2Bytes = Source.fromFile(necsx2Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16).toInt).toArray
    var necsx2Cycles = 0L
    var necsx2Insts = 0L
    var necsx2Reads = 0L
    var necsx2Writes = 0L

    simulate(new NecSx2Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- necsx2Bytes.indices) mem(i) = necsx2Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      necsx2Cycles = c.io.pmu_cycles.peek().litValue.toLong
      necsx2Insts  = c.io.pmu_insts.peek().litValue.toLong
      necsx2Reads  = c.io.pmu_reads.peek().litValue.toLong
      necsx2Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(92) shouldBe 11
      mem(93) shouldBe 22
      mem(94) shouldBe 33
      mem(95) shouldBe 44
    }

    // 64. IBM System/370 Vector Facility
    val ibms370vfHex = findWorkspaceFile("ibms370vf/sw/test_vector.hex")
    val ibms370vfBytes = Source.fromFile(ibms370vfHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16).toInt).toArray
    var ibms370vfCycles = 0L
    var ibms370vfInsts = 0L
    var ibms370vfReads = 0L
    var ibms370vfWrites = 0L

    simulate(new IbmS370VfCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- ibms370vfBytes.indices) mem(i) = ibms370vfBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      ibms370vfCycles = c.io.pmu_cycles.peek().litValue.toLong
      ibms370vfInsts  = c.io.pmu_insts.peek().litValue.toLong
      ibms370vfReads  = c.io.pmu_reads.peek().litValue.toLong
      ibms370vfWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(92) shouldBe 11
      mem(93) shouldBe 22
      mem(94) shouldBe 33
      mem(95) shouldBe 44
    }

    // 65. IBM 801
    val ibm801Hex = findWorkspaceFile("ibm801/sw/test_vector.hex")
    val ibm801Bytes = Source.fromFile(ibm801Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16).toInt).toArray
    var ibm801Cycles = 0L
    var ibm801Insts = 0L
    var ibm801Reads = 0L
    var ibm801Writes = 0L

    simulate(new Ibm801Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- ibm801Bytes.indices) mem(i) = ibm801Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      ibm801Cycles = c.io.pmu_cycles.peek().litValue.toLong
      ibm801Insts  = c.io.pmu_insts.peek().litValue.toLong
      ibm801Reads  = c.io.pmu_reads.peek().litValue.toLong
      ibm801Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 66. SPARC
    val sparcHex = findWorkspaceFile("sparc/sw/test_vector.hex")
    val sparcBytes = Source.fromFile(sparcHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16).toInt).toArray
    var sparcCycles = 0L
    var sparcInsts = 0L
    var sparcReads = 0L
    var sparcWrites = 0L

    simulate(new SparcCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- sparcBytes.indices) mem(i) = sparcBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      sparcCycles = c.io.pmu_cycles.peek().litValue.toLong
      sparcInsts  = c.io.pmu_insts.peek().litValue.toLong
      sparcReads  = c.io.pmu_reads.peek().litValue.toLong
      sparcWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 67. PowerPC
    val powerpcHex = findWorkspaceFile("powerpc/sw/test_vector.hex")
    val powerpcBytes = Source.fromFile(powerpcHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16).toInt).toArray
    var powerpcCycles = 0L
    var powerpcInsts = 0L
    var powerpcReads = 0L
    var powerpcWrites = 0L

    simulate(new PowerpcCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- powerpcBytes.indices) mem(i) = powerpcBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      powerpcCycles = c.io.pmu_cycles.peek().litValue.toLong
      powerpcInsts  = c.io.pmu_insts.peek().litValue.toLong
      powerpcReads  = c.io.pmu_reads.peek().litValue.toLong
      powerpcWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 68. JVM
    val jvmHex = findWorkspaceFile("jvm/sw/test_vector.hex")
    val jvmBytes = Source.fromFile(jvmHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16).toInt).toArray
    var jvmCycles = 0L
    var jvmInsts = 0L
    var jvmReads = 0L
    var jvmWrites = 0L

    simulate(new JvmCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- jvmBytes.indices) mem(i) = jvmBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      jvmCycles = c.io.pmu_cycles.peek().litValue.toLong
      jvmInsts  = c.io.pmu_insts.peek().litValue.toLong
      jvmReads  = c.io.pmu_reads.peek().litValue.toLong
      jvmWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 69. Setun
    val setunHex = findWorkspaceFile("setun/sw/test_vector.hex")
    val setunBytes = Source.fromFile(setunHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16).toInt).toArray
    var setunCycles = 0L
    var setunInsts = 0L
    var setunReads = 0L
    var setunWrites = 0L

    simulate(new SetunCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- setunBytes.indices) mem(i) = setunBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      setunCycles = c.io.pmu_cycles.peek().litValue.toLong
      setunInsts  = c.io.pmu_insts.peek().litValue.toLong
      setunReads  = c.io.pmu_reads.peek().litValue.toLong
      setunWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 70. IBM 1620
    val ibm1620Hex = findWorkspaceFile("ibm1620/sw/test_vector.hex")
    val ibm1620Bytes = Source.fromFile(ibm1620Hex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16).toInt).toArray
    var ibm1620Cycles = 0L
    var ibm1620Insts = 0L
    var ibm1620Reads = 0L
    var ibm1620Writes = 0L

    simulate(new Ibm1620Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- ibm1620Bytes.indices) mem(i) = ibm1620Bytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          if (addr >= 300 && addr < 1000) {
            c.io.mem.rdata.poke((addr - 300).U)
          } else {
            c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
          }
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      ibm1620Cycles = c.io.pmu_cycles.peek().litValue.toLong
      ibm1620Insts  = c.io.pmu_insts.peek().litValue.toLong
      ibm1620Reads  = c.io.pmu_reads.peek().litValue.toLong
      ibm1620Writes = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 71. Symbolics 3600
    val symbolicsHex = findWorkspaceFile("symbolics3600/sw/test_vector.hex")
    val symbolicsBytes = Source.fromFile(symbolicsHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16).toInt).toArray
    var symbolicsCycles = 0L
    var symbolicsInsts = 0L
    var symbolicsReads = 0L
    var symbolicsWrites = 0L

    simulate(new Symbolics3600Core) { c =>
      val mem = Array.fill(256)(0)
      for (i <- symbolicsBytes.indices) mem(i) = symbolicsBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      symbolicsCycles = c.io.pmu_cycles.peek().litValue.toLong
      symbolicsInsts  = c.io.pmu_insts.peek().litValue.toLong
      symbolicsReads  = c.io.pmu_reads.peek().litValue.toLong
      symbolicsWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 72. MIT Dataflow
    val mitdataflowHex = findWorkspaceFile("mitdataflow/sw/test_vector.hex")
    val mitdataflowBytes = Source.fromFile(mitdataflowHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16).toInt).toArray
    var mitdataflowCycles = 0L
    var mitdataflowInsts = 0L
    var mitdataflowReads = 0L
    var mitdataflowWrites = 0L

    simulate(new MitDataflowCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- mitdataflowBytes.indices) mem(i) = mitdataflowBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      mitdataflowCycles = c.io.pmu_cycles.peek().litValue.toLong
      mitdataflowInsts  = c.io.pmu_insts.peek().litValue.toLong
      mitdataflowReads  = c.io.pmu_reads.peek().litValue.toLong
      mitdataflowWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 73. SUBLEQ
    val subleqHex = findWorkspaceFile("subleq/sw/test_vector.hex")
    val subleqBytes = Source.fromFile(subleqHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16).toInt).toArray
    var subleqCycles = 0L
    var subleqInsts = 0L
    var subleqReads = 0L
    var subleqWrites = 0L

    simulate(new SubleqCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- subleqBytes.indices) mem(i) = subleqBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 1000
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      subleqCycles = c.io.pmu_cycles.peek().litValue.toLong
      subleqInsts  = c.io.pmu_insts.peek().litValue.toLong
      subleqReads  = c.io.pmu_reads.peek().litValue.toLong
      subleqWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 74. SOAR
    val soarHex = findWorkspaceFile("soar/sw/test_vector.hex")
    val soarBytes = Source.fromFile(soarHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16).toInt).toArray
    var soarCycles = 0L
    var soarInsts = 0L
    var soarReads = 0L
    var soarWrites = 0L

    simulate(new SoarCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- soarBytes.indices) mem(i) = soarBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      soarCycles = c.io.pmu_cycles.peek().litValue.toLong
      soarInsts  = c.io.pmu_insts.peek().litValue.toLong
      soarReads  = c.io.pmu_reads.peek().litValue.toLong
      soarWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // 75. TTA
    val ttaHex = findWorkspaceFile("tta/sw/test_vector.hex")
    val ttaBytes = Source.fromFile(ttaHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16).toInt).toArray
    var ttaCycles = 0L
    var ttaInsts = 0L
    var ttaReads = 0L
    var ttaWrites = 0L

    simulate(new TtaCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- ttaBytes.indices) mem(i) = ttaBytes(i)
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var limit = 500
      var halted = false
      while (limit > 0 && !halted) {
        val req = c.io.mem.req.peek().litToBoolean
        val addr = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) mem(addr) = wdata
          c.io.mem.rdata.poke((mem(addr).toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        limit -= 1
        if (c.io.hlt.peek().litToBoolean) halted = true
      }
      ttaCycles = c.io.pmu_cycles.peek().litValue.toLong
      ttaInsts  = c.io.pmu_insts.peek().litValue.toLong
      ttaReads  = c.io.pmu_reads.peek().litValue.toLong
      ttaWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }

    // Print Consolidated Comparative Table
    def aluDutyCycle(arch: String, cycles: Long): String = {
      val aluCycles: Double = arch match {
        case "cray" => 4.0
        case "mali200" => 1.0
        case "amdr600" => 4.0
        case "voodoo1" => 4.0
        case "geforce256" => 4.0
        case "radeonr100" => 4.0
        case "powervr1" => 4.0
        case "mips1" | "arm1" | "berkeleyrisc" | "ibm6150" | "hp3000" | "ethlilith" | "ibm801" | "sparc" | "powerpc" | "jvm" | "soar" => 8.0
        case "b5500" | "ucsdp" => 6.0
        case "mos" | "intel8080a" | "motorola6800" | "pdp8" | "upd7720" | "tms32010" | "adsp2100" | "ibmmwave" | "amd2901" | "intel3002" | "imp16" | "mc10800" | "icldap" | "goodmpp" | "cm1" => 4.0
        case "illiac4" | "ibmmfast" => 8.0
        case "multiflow" | "cydra5" | "tms320c6k" | "crusoe" | "itanium" => 4.0
        case "cdcstar100" | "tiasc" | "convexc1" | "necsx2" | "ibms370vf" => 4.0
        case _ => 4.0
      }
      if (cycles > 0) String.format("%.1f%%", Double.box((aluCycles / cycles.toDouble) * 100.0)) else "N/A"
    }

    def memBwEfficiency(wordWidth: Double, reads: Long, writes: Long, insts: Long): String = {
      if (insts > 0) {
        val totalBytes = (reads + writes) * (wordWidth / 8.0)
        String.format("%.2f B/inst", Double.box(totalBytes / insts.toDouble))
      } else "N/A"
    }

    def regPortStress(arch: String): String = {
      val factor = arch match {
        case "cray" | "mali200" | "amdr600" | "geforce256" | "radeonr100" | "voodoo1" | "powervr1" => 4.5
        case "illiac4" | "ibmmfast" => 4.5
        case "mips1" | "arm1" | "berkeleyrisc" | "ibm6150" | "decvax" | "pdp11" | "m68k" | "ibm801" | "sparc" | "powerpc" | "soar" => 2.5
        case "b5500" | "ucsdp" | "hp3000" | "ethlilith" | "cambridgeedsac" | "manchestermu1" | "babbage" | "harvard" | "zuse" | "univac" | "princetonias" | "jvm" | "setun" | "symbolics3600" | "mitdataflow" | "subleq" | "tta" | "ibm1620" => 0.5
        case "multiflow" | "cydra5" | "tms320c6k" | "crusoe" | "itanium" => 3.0
        case "cdcstar100" | "tiasc" | "convexc1" | "necsx2" | "ibms370vf" => 4.0
        case _ => 1.0
      }
      String.format("%.1f regs/inst", Double.box(factor))
    }

    val pdp8Cpi  = if (pdp8Insts > 0)  String.format("%.2f", Double.box(pdp8Cycles.toDouble / pdp8Insts))  else "N/A"
    val pdp11Cpi = if (pdp11Insts > 0) String.format("%.2f", Double.box(pdp11Cycles.toDouble / pdp11Insts)) else "N/A"
    val mosCpi   = if (mosInsts > 0)   String.format("%.2f", Double.box(mosCycles.toDouble / mosInsts))   else "N/A"
    val ibmCpi   = if (ibmInsts > 0)   String.format("%.2f", Double.box(ibmCycles.toDouble / ibmInsts))   else "N/A"
    val m68kCpi  = if (m68kInsts > 0)  String.format("%.2f", Double.box(m68kCycles.toDouble / m68kInsts))  else "N/A"
    val b5500Cpi = if (b5500Insts > 0) String.format("%.2f", Double.box(b5500Cycles.toDouble / b5500Insts)) else "N/A"
    val cdcCpi   = if (cdcInsts > 0)   String.format("%.2f", Double.box(cdcCycles.toDouble / cdcInsts))   else "N/A"
    val crayCpi  = if (crayInsts > 0)  String.format("%.2f", Double.box(crayCycles.toDouble / crayInsts))  else "N/A"
    val babbageCpi = if (babbageInsts > 0) String.format("%.2f", Double.box(babbageCycles.toDouble / babbageInsts)) else "N/A"
    val harvardCpi = if (harvardInsts > 0) String.format("%.2f", Double.box(harvardCycles.toDouble / harvardInsts)) else "N/A"
    val zuseCpi    = if (zuseInsts > 0)    String.format("%.2f", Double.box(zuseCycles.toDouble / zuseInsts))       else "N/A"
    val manchestermu1Cpi = if (manchestermu1Insts > 0) String.format("%.2f", Double.box(manchestermu1Cycles.toDouble / manchestermu1Insts)) else "N/A"
    val univacCpi  = if (univacInsts > 0)  String.format("%.2f", Double.box(univacCycles.toDouble / univacInsts))   else "N/A"
    val princetoniasCpi     = if (princetoniasInsts > 0)     String.format("%.2f", Double.box(princetoniasCycles.toDouble / princetoniasInsts))       else "N/A"
    val cambridgeedsacCpi   = if (cambridgeedsacInsts > 0)   String.format("%.2f", Double.box(cambridgeedsacCycles.toDouble / cambridgeedsacInsts))   else "N/A"
    val ibm701Cpi  = if (ibm701Insts > 0)  String.format("%.2f", Double.box(ibm701Cycles.toDouble / ibm701Insts))   else "N/A"
    val ibm704Cpi  = if (ibm704Insts > 0)  String.format("%.2f", Double.box(ibm704Cycles.toDouble / ibm704Insts))   else "N/A"
    val ibm650Cpi  = if (ibm650Insts > 0)  String.format("%.2f", Double.box(ibm650Cycles.toDouble / ibm650Insts))   else "N/A"
    val ibm705Cpi  = if (ibm705Insts > 0)  String.format("%.2f", Double.box(ibm705Cycles.toDouble / ibm705Insts))   else "N/A"
    val ibm1401Cpi = if (ibm1401Insts > 0) String.format("%.2f", Double.box(ibm1401Cycles.toDouble / ibm1401Insts)) else "N/A"
    val stczebraCpi = if (stczebraInsts > 0) String.format("%.2f", Double.box(stczebraCycles.toDouble / stczebraInsts)) else "N/A"
    val bullgammaCpi = if (bullgammaInsts > 0) String.format("%.2f", Double.box(bullgammaCycles.toDouble / bullgammaInsts)) else "N/A"
    val ibmstretchCpi = if (ibmstretchInsts > 0) String.format("%.2f", Double.box(ibmstretchCycles.toDouble / ibmstretchInsts)) else "N/A"
    val univac1103aCpi = if (univac1103aInsts > 0) String.format("%.2f", Double.box(univac1103aCycles.toDouble / univac1103aInsts)) else "N/A"
    val cdc6600ppuCpi  = if (cdc6600ppuInsts > 0)  String.format("%.2f", Double.box(cdc6600ppuCycles.toDouble / cdc6600ppuInsts))  else "N/A"
    val decvaxCpi = if (decvaxInsts > 0) String.format("%.2f", Double.box(decvaxCycles.toDouble / decvaxInsts)) else "N/A"
    val intel8080aCpi = if (intel8080aInsts > 0) String.format("%.2f", Double.box(intel8080aCycles.toDouble / intel8080aInsts)) else "N/A"
    val motorola6800Cpi = if (motorola6800Insts > 0) String.format("%.2f", Double.box(motorola6800Cycles.toDouble / motorola6800Insts)) else "N/A"
    val ibm6150Cpi = if (ibm6150Insts > 0) String.format("%.2f", Double.box(ibm6150Cycles.toDouble / ibm6150Insts)) else "N/A"
    val mips1Cpi = if (mips1Insts > 0) String.format("%.2f", Double.box(mips1Cycles.toDouble / mips1Insts)) else "N/A"
    val arm1Cpi = if (arm1Insts > 0) String.format("%.2f", Double.box(arm1Cycles.toDouble / arm1Insts)) else "N/A"
    val berkeleyriscCpi = if (berkeleyriscInsts > 0) String.format("%.2f", Double.box(berkeleyriscCycles.toDouble / berkeleyriscInsts)) else "N/A"
    val hp3000Cpi = if (hp3000Insts > 0) String.format("%.2f", Double.box(hp3000Cycles.toDouble / hp3000Insts)) else "N/A"
    val ethlilithCpi = if (ethlilithInsts > 0) String.format("%.2f", Double.box(ethlilithCycles.toDouble / ethlilithInsts)) else "N/A"
    val ucsdpCpi = if (ucsdpInsts > 0) String.format("%.2f", Double.box(ucsdpCycles.toDouble / ucsdpInsts)) else "N/A"
    val upd7720Cpi = if (upd7720Insts > 0) String.format("%.2f", Double.box(upd7720Cycles.toDouble / upd7720Insts)) else "N/A"
    val tms32010Cpi = if (tms32010Insts > 0) String.format("%.2f", Double.box(tms32010Cycles.toDouble / tms32010Insts)) else "N/A"
    val adsp2100Cpi = if (adsp2100Insts > 0) String.format("%.2f", Double.box(adsp2100Cycles.toDouble / adsp2100Insts)) else "N/A"
    val ibmmwaveCpi = if (ibmmwaveInsts > 0) String.format("%.2f", Double.box(ibmmwaveCycles.toDouble / ibmmwaveInsts)) else "N/A"
    val voodoo1Cpi = if (voodoo1Insts > 0) String.format("%.2f", Double.box(voodoo1Cycles.toDouble / voodoo1Insts)) else "N/A"
    val geforce256Cpi = if (geforce256Insts > 0) String.format("%.2f", Double.box(geforce256Cycles.toDouble / geforce256Insts)) else "N/A"
    val radeonr100Cpi = if (radeonr100Insts > 0) String.format("%.2f", Double.box(radeonr100Cycles.toDouble / radeonr100Insts)) else "N/A"
    val powervr1Cpi = if (powervr1Insts > 0) String.format("%.2f", Double.box(powervr1Cycles.toDouble / powervr1Insts)) else "N/A"
    val mali200Cpi = if (mali200Insts > 0) String.format("%.2f", Double.box(mali200Cycles.toDouble / mali200Insts)) else "N/A"
    val amdr600Cpi = if (amdr600Insts > 0) String.format("%.2f", Double.box(amdr600Cycles.toDouble / amdr600Insts)) else "N/A"
    val amd2901Cpi = if (amd2901Insts > 0) String.format("%.2f", Double.box(amd2901Cycles.toDouble / amd2901Insts)) else "N/A"
    val intel3002Cpi = if (intel3002Insts > 0) String.format("%.2f", Double.box(intel3002Cycles.toDouble / intel3002Insts)) else "N/A"
    val imp16Cpi = if (imp16Insts > 0) String.format("%.2f", Double.box(imp16Cycles.toDouble / imp16Insts)) else "N/A"
    val mc10800Cpi = if (mc10800Insts > 0) String.format("%.2f", Double.box(mc10800Cycles.toDouble / mc10800Insts)) else "N/A"
    val illiac4Cpi = if (illiac4Insts > 0) String.format("%.2f", Double.box(illiac4Cycles.toDouble / illiac4Insts)) else "N/A"
    val icldapCpi = if (icldapInsts > 0) String.format("%.2f", Double.box(icldapCycles.toDouble / icldapInsts)) else "N/A"
    val goodmppCpi = if (goodmppInsts > 0) String.format("%.2f", Double.box(goodmppCycles.toDouble / goodmppInsts)) else "N/A"
    val cm1Cpi = if (cm1Insts > 0) String.format("%.2f", Double.box(cm1Cycles.toDouble / cm1Insts)) else "N/A"
    val ibmmfastCpi = if (ibmmfastInsts > 0) String.format("%.2f", Double.box(ibmmfastCycles.toDouble / ibmmfastInsts)) else "N/A"
    val multiflowCpi = if (multiflowInsts > 0) String.format("%.2f", Double.box(multiflowCycles.toDouble / multiflowInsts)) else "N/A"
    val cydra5Cpi    = if (cydra5Insts > 0)    String.format("%.2f", Double.box(cydra5Cycles.toDouble / cydra5Insts))       else "N/A"
    val tms320c6kCpi = if (tms320c6kInsts > 0) String.format("%.2f", Double.box(tms320c6kCycles.toDouble / tms320c6kInsts)) else "N/A"
    val crusoeCpi    = if (crusoeInsts > 0)    String.format("%.2f", Double.box(crusoeCycles.toDouble / crusoeInsts))       else "N/A"
    val itaniumCpi   = if (itaniumInsts > 0)   String.format("%.2f", Double.box(itaniumCycles.toDouble / itaniumInsts))     else "N/A"

    val cdcstar100Cpi = if (cdcstar100Insts > 0) String.format("%.2f", Double.box(cdcstar100Cycles.toDouble / cdcstar100Insts)) else "N/A"
    val tiascCpi      = if (tiascInsts > 0)      String.format("%.2f", Double.box(tiascCycles.toDouble / tiascInsts))           else "N/A"
    val convexc1Cpi   = if (convexc1Insts > 0)   String.format("%.2f", Double.box(convexc1Cycles.toDouble / convexc1Insts))     else "N/A"
    val necsx2Cpi     = if (necsx2Insts > 0)     String.format("%.2f", Double.box(necsx2Cycles.toDouble / necsx2Insts))         else "N/A"
    val ibms370vfCpi  = if (ibms370vfInsts > 0)  String.format("%.2f", Double.box(ibms370vfCycles.toDouble / ibms370vfInsts))   else "N/A"
    val ibm801Cpi     = if (ibm801Insts > 0)     String.format("%.2f", Double.box(ibm801Cycles.toDouble / ibm801Insts))         else "N/A"
    val sparcCpi      = if (sparcInsts > 0)      String.format("%.2f", Double.box(sparcCycles.toDouble / sparcInsts))           else "N/A"
    val powerpcCpi    = if (powerpcInsts > 0)    String.format("%.2f", Double.box(powerpcCycles.toDouble / powerpcInsts))       else "N/A"
    val jvmCpi        = if (jvmInsts > 0)        String.format("%.2f", Double.box(jvmCycles.toDouble / jvmInsts))               else "N/A"
    val setunCpi      = if (setunInsts > 0)      String.format("%.2f", Double.box(setunCycles.toDouble / setunInsts))           else "N/A"
    val ibm1620Cpi    = if (ibm1620Insts > 0)    String.format("%.2f", Double.box(ibm1620Cycles.toDouble / ibm1620Insts))       else "N/A"
    val symbolicsCpi  = if (symbolicsInsts > 0)  String.format("%.2f", Double.box(symbolicsCycles.toDouble / symbolicsInsts))   else "N/A"
    val mitdataflowCpi = if (mitdataflowInsts > 0) String.format("%.2f", Double.box(mitdataflowCycles.toDouble / mitdataflowInsts)) else "N/A"
    val subleqCpi     = if (subleqInsts > 0)     String.format("%.2f", Double.box(subleqCycles.toDouble / subleqInsts))         else "N/A"
    val soarCpi       = if (soarInsts > 0)       String.format("%.2f", Double.box(soarCycles.toDouble / soarInsts))             else "N/A"
    val ttaCpi        = if (ttaInsts > 0)        String.format("%.2f", Double.box(ttaCycles.toDouble / ttaInsts))               else "N/A"

    val rows = Seq(
      s"| Babbage Anal. Eng.  | 64                | $babbageCycles              | $babbageInsts                   | $babbageReads            | $babbageWrites             | $babbageCpi | ${babbageBytes.length} | ${aluDutyCycle("babbage", babbageCycles)} | ${memBwEfficiency(64.0, babbageReads, babbageWrites, babbageInsts)} | ${regPortStress("babbage")} |",
      s"| Harvard Mark I      | 64                | $harvardCycles              | $harvardInsts                   | $harvardReads            | $harvardWrites             | $harvardCpi | ${harvardBytes.length} | ${aluDutyCycle("harvard", harvardCycles)} | ${memBwEfficiency(64.0, harvardReads, harvardWrites, harvardInsts)} | ${regPortStress("harvard")} |",
      s"| Zuse Z1             | 22                | $zuseCycles              | $zuseInsts                   | $zuseReads            | $zuseWrites             | $zuseCpi | ${zuseBytes.length} | ${aluDutyCycle("zuse", zuseCycles)} | ${memBwEfficiency(22.0, zuseReads, zuseWrites, zuseInsts)} | ${regPortStress("zuse")} |",
      s"| Manchester Baby     | 32                | $manchestermu1Cycles              | $manchestermu1Insts                   | $manchestermu1Reads            | $manchestermu1Writes             | $manchestermu1Cpi | ${manchestermu1Bytes.length} | ${aluDutyCycle("manchestermu1", manchestermu1Cycles)} | ${memBwEfficiency(32.0, manchestermu1Reads, manchestermu1Writes, manchestermu1Insts)} | ${regPortStress("manchestermu1")} |",
      s"| Univac I            | 72                | $univacCycles              | $univacInsts                   | $univacReads            | $univacWrites             | $univacCpi | ${univacBytes.length} | ${aluDutyCycle("univac", univacCycles)} | ${memBwEfficiency(72.0, univacReads, univacWrites, univacInsts)} | ${regPortStress("univac")} |",
      s"| Princeton IAS       | 40                | $princetoniasCycles              | $princetoniasInsts                   | $princetoniasReads            | $princetoniasWrites             | $princetoniasCpi | ${princetoniasBytes.length} | ${aluDutyCycle("princetonias", princetoniasCycles)} | ${memBwEfficiency(40.0, princetoniasReads, princetoniasWrites, princetoniasInsts)} | ${regPortStress("princetonias")} |",
      s"| EDSAC               | 17                | $cambridgeedsacCycles              | $cambridgeedsacInsts                   | $cambridgeedsacReads            | $cambridgeedsacWrites             | $cambridgeedsacCpi | ${cambridgeedsacBytes.length} | ${aluDutyCycle("cambridgeedsac", cambridgeedsacCycles)} | ${memBwEfficiency(17.0, cambridgeedsacReads, cambridgeedsacWrites, cambridgeedsacInsts)} | ${regPortStress("cambridgeedsac")} |",
      s"| IBM 701             | 36                | $ibm701Cycles              | $ibm701Insts                   | $ibm701Reads            | $ibm701Writes             | $ibm701Cpi | ${ibm701Bytes.length} | ${aluDutyCycle("ibm701", ibm701Cycles)} | ${memBwEfficiency(36.0, ibm701Reads, ibm701Writes, ibm701Insts)} | ${regPortStress("ibm701")} |",
      s"| IBM 704             | 36                | $ibm704Cycles              | $ibm704Insts                   | $ibm704Reads            | $ibm704Writes             | $ibm704Cpi | ${ibm704Bytes.length} | ${aluDutyCycle("ibm704", ibm704Cycles)} | ${memBwEfficiency(36.0, ibm704Reads, ibm704Writes, ibm704Insts)} | ${regPortStress("ibm704")} |",
      s"| IBM 650             | 40                | $ibm650Cycles              | $ibm650Insts                   | $ibm650Reads            | $ibm650Writes             | $ibm650Cpi | ${ibm650Bytes.length} | ${aluDutyCycle("ibm650", ibm650Cycles)} | ${memBwEfficiency(40.0, ibm650Reads, ibm650Writes, ibm650Insts)} | ${regPortStress("ibm650")} |",
      s"| IBM 705             | 35                | $ibm705Cycles              | $ibm705Insts                   | $ibm705Reads            | $ibm705Writes             | $ibm705Cpi | ${ibm705Bytes.length} | ${aluDutyCycle("ibm705", ibm705Cycles)} | ${memBwEfficiency(35.0, ibm705Reads, ibm705Writes, ibm705Insts)} | ${regPortStress("ibm705")} |",
      s"| IBM 1401            | 36                | $ibm1401Cycles              | $ibm1401Insts                   | $ibm1401Reads            | $ibm1401Writes             | $ibm1401Cpi | ${ibm1401Bytes.length} | ${aluDutyCycle("ibm1401", ibm1401Cycles)} | ${memBwEfficiency(36.0, ibm1401Reads, ibm1401Writes, ibm1401Insts)} | ${regPortStress("ibm1401")} |",
      s"| STC ZEBRA           | 33                | $stczebraCycles              | $stczebraInsts                   | $stczebraReads            | $stczebraWrites             | $stczebraCpi | ${stczebraBytes.length} | ${aluDutyCycle("stczebra", stczebraCycles)} | ${memBwEfficiency(33.0, stczebraReads, stczebraWrites, stczebraInsts)} | ${regPortStress("stczebra")} |",
      s"| Bull Gamma 60        | 24                | $bullgammaCycles              | $bullgammaInsts                   | $bullgammaReads            | $bullgammaWrites             | $bullgammaCpi | ${bullgammaBytes.length} | ${aluDutyCycle("bullgamma", bullgammaCycles)} | ${memBwEfficiency(24.0, bullgammaReads, bullgammaWrites, bullgammaInsts)} | ${regPortStress("bullgamma")} |",
      s"| IBM Stretch         | 64                | $ibmstretchCycles              | $ibmstretchInsts                   | $ibmstretchReads            | $ibmstretchWrites             | $ibmstretchCpi | ${ibmstretchBytes.length} | ${aluDutyCycle("ibmstretch", ibmstretchCycles)} | ${memBwEfficiency(64.0, ibmstretchReads, ibmstretchWrites, ibmstretchInsts)} | ${regPortStress("ibmstretch")} |",
      s"| MOS 6502            | 8                 | $mosCycles              | $mosInsts                   | $mosReads            | $mosWrites             | $mosCpi | ${mosBytes.length} | ${aluDutyCycle("mos", mosCycles)} | ${memBwEfficiency(8.0, mosReads, mosWrites, mosInsts)} | ${regPortStress("mos")} |",
      s"| DEC PDP-8           | 12                | $pdp8Cycles              | $pdp8Insts                   | $pdp8Reads            | $pdp8Writes             | $pdp8Cpi | ${pdp8Bytes.length} | ${aluDutyCycle("pdp8", pdp8Cycles)} | ${memBwEfficiency(12.0, pdp8Reads, pdp8Writes, pdp8Insts)} | ${regPortStress("pdp8")} |",
      s"| DEC PDP-11          | 16                | $pdp11Cycles              | $pdp11Insts                   | $pdp11Reads            | $pdp11Writes             | $pdp11Cpi | ${pdp11Bytes.length} | ${aluDutyCycle("pdp11", pdp11Cycles)} | ${memBwEfficiency(16.0, pdp11Reads, pdp11Writes, pdp11Insts)} | ${regPortStress("pdp11")} |",
      s"| IBM System/360      | 32                | $ibmCycles              | $ibmInsts                   | $ibmReads            | $ibmWrites             | $ibmCpi | ${ibmBytes.length} | ${aluDutyCycle("ibm360", ibmCycles)} | ${memBwEfficiency(32.0, ibmReads, ibmWrites, ibmInsts)} | ${regPortStress("ibm360")} |",
      s"| Motorola 68000      | 32                | $m68kCycles              | $m68kInsts                   | $m68kReads            | $m68kWrites             | $m68kCpi | ${m68kBytes.length} | ${aluDutyCycle("m68k", m68kCycles)} | ${memBwEfficiency(32.0, m68kReads, m68kWrites, m68kInsts)} | ${regPortStress("m68k")} |",
      s"| Burroughs B5500     | 48                | $b5500Cycles              | $b5500Insts                   | $b5500Reads            | $b5500Writes             | $b5500Cpi | ${b5500Bytes.length} | ${aluDutyCycle("b5500", b5500Cycles)} | ${memBwEfficiency(48.0, b5500Reads, b5500Writes, b5500Insts)} | ${regPortStress("b5500")} |",
      s"| CDC 6600            | 60                | $cdcCycles              | $cdcInsts                   | $cdcReads            | $cdcWrites             | $cdcCpi | ${cdcBytes.length} | ${aluDutyCycle("cdc", cdcCycles)} | ${memBwEfficiency(60.0, cdcReads, cdcWrites, cdcInsts)} | ${regPortStress("cdc")} |",
      s"| Cray-1              | 64 (Vector)       | $crayCycles              | $crayInsts                   | $crayReads            | $crayWrites             | $crayCpi | ${crayBytes.length} | ${aluDutyCycle("cray", crayCycles)} | ${memBwEfficiency(64.0, crayReads, crayWrites, crayInsts)} | ${regPortStress("cray")} |",
      s"| Univac 1103A        | 36                | $univac1103aCycles              | $univac1103aInsts                   | $univac1103aReads            | $univac1103aWrites             | $univac1103aCpi | ${univac1103aBytes.length} | ${aluDutyCycle("univac1103a", univac1103aCycles)} | ${memBwEfficiency(36.0, univac1103aReads, univac1103aWrites, univac1103aInsts)} | ${regPortStress("univac1103a")} |",
      s"| CDC 6600 PPU        | 12                | $cdc6600ppuCycles              | $cdc6600ppuInsts                   | $cdc6600ppuReads            | $cdc6600ppuWrites             | $cdc6600ppuCpi | ${cdc6600ppuBytes.length} | ${aluDutyCycle("cdc6600ppu", cdc6600ppuCycles)} | ${memBwEfficiency(12.0, cdc6600ppuReads, cdc6600ppuWrites, cdc6600ppuInsts)} | ${regPortStress("cdc6600ppu")} |",
      s"| DEC VAX             | 32                | $decvaxCycles              | $decvaxInsts                   | $decvaxReads            | $decvaxWrites             | $decvaxCpi | ${decvaxBytes.length} | ${aluDutyCycle("decvax", decvaxCycles)} | ${memBwEfficiency(32.0, decvaxReads, decvaxWrites, decvaxInsts)} | ${regPortStress("decvax")} |",
      s"| Intel 8080A         | 8                 | $intel8080aCycles              | $intel8080aInsts                   | $intel8080aReads            | $intel8080aWrites             | $intel8080aCpi | ${intel8080aBytes.length} | ${aluDutyCycle("intel8080a", intel8080aCycles)} | ${memBwEfficiency(8.0, intel8080aReads, intel8080aWrites, intel8080aInsts)} | ${regPortStress("intel8080a")} |",
      s"| Motorola 6800       | 8                 | $motorola6800Cycles              | $motorola6800Insts                   | $motorola6800Reads            | $motorola6800Writes             | $motorola6800Cpi | ${motorola6800Bytes.length} | ${aluDutyCycle("motorola6800", motorola6800Cycles)} | ${memBwEfficiency(8.0, motorola6800Reads, motorola6800Writes, motorola6800Insts)} | ${regPortStress("motorola6800")} |",
      s"| IBM 6150 ROMP       | 32                | $ibm6150Cycles              | $ibm6150Insts                   | $ibm6150Reads            | $ibm6150Writes             | $ibm6150Cpi | ${ibm6150Bytes.length} | ${aluDutyCycle("ibm6150", ibm6150Cycles)} | ${memBwEfficiency(32.0, ibm6150Reads, ibm6150Writes, ibm6150Insts)} | ${regPortStress("ibm6150")} |",
      s"| MIPS I (R2000)      | 32                | $mips1Cycles              | $mips1Insts                   | $mips1Reads            | $mips1Writes             | $mips1Cpi | ${mips1Bytes.length} | ${aluDutyCycle("mips1", mips1Cycles)} | ${memBwEfficiency(32.0, mips1Reads, mips1Writes, mips1Insts)} | ${regPortStress("mips1")} |",
      s"| ARM1                | 32                | $arm1Cycles              | $arm1Insts                   | $arm1Reads            | $arm1Writes             | $arm1Cpi | ${arm1Bytes.length} | ${aluDutyCycle("arm1", arm1Cycles)} | ${memBwEfficiency(32.0, arm1Reads, arm1Writes, arm1Insts)} | ${regPortStress("arm1")} |",
      s"| Berkeley RISC-I     | 32                | $berkeleyriscCycles              | $berkeleyriscInsts                   | $berkeleyriscReads            | $berkeleyriscWrites             | $berkeleyriscCpi | ${berkeleyriscBytes.length} | ${aluDutyCycle("berkeleyrisc", berkeleyriscCycles)} | ${memBwEfficiency(32.0, berkeleyriscReads, berkeleyriscWrites, berkeleyriscInsts)} | ${regPortStress("berkeleyrisc")} |",
      s"| HP 3000             | 16                | $hp3000Cycles              | $hp3000Insts                   | $hp3000Reads            | $hp3000Writes             | $hp3000Cpi | ${hp3000Bytes.length} | ${aluDutyCycle("hp3000", hp3000Cycles)} | ${memBwEfficiency(16.0, hp3000Reads, hp3000Writes, hp3000Insts)} | ${regPortStress("hp3000")} |",
      s"| Ethlilith           | 16                | $ethlilithCycles              | $ethlilithInsts                   | $ethlilithReads            | $ethlilithWrites             | $ethlilithCpi | ${ethlilithBytes.length} | ${aluDutyCycle("ethlilith", ethlilithCycles)} | ${memBwEfficiency(16.0, ethlilithReads, ethlilithWrites, ethlilithInsts)} | ${regPortStress("ethlilith")} |",
      s"| UCSD Pascal P-Mach  | 16                | $ucsdpCycles              | $ucsdpInsts                   | $ucsdpReads            | $ucsdpWrites             | $ucsdpCpi | ${ucsdpBytes.length} | ${aluDutyCycle("ucsdp", ucsdpCycles)} | ${memBwEfficiency(16.0, ucsdpReads, ucsdpWrites, ucsdpInsts)} | ${regPortStress("ucsdp")} |",
      s"| NEC uPD7720 DSP     | 16                | $upd7720Cycles              | $upd7720Insts                   | $upd7720Reads            | $upd7720Writes             | $upd7720Cpi | ${upd7720Bytes.length} | ${aluDutyCycle("upd7720", upd7720Cycles)} | ${memBwEfficiency(16.0, upd7720Reads, upd7720Writes, upd7720Insts)} | ${regPortStress("upd7720")} |",
      s"| TI TMS32010 DSP     | 16                | $tms32010Cycles              | $tms32010Insts                   | $tms32010Reads            | $tms32010Writes             | $tms32010Cpi | ${tms32010Bytes.length} | ${aluDutyCycle("tms32010", tms32010Cycles)} | ${memBwEfficiency(16.0, tms32010Reads, tms32010Writes, tms32010Insts)} | ${regPortStress("tms32010")} |",
      s"| ADI ADSP-2100 DSP   | 16                | $adsp2100Cycles              | $adsp2100Insts                   | $adsp2100Reads            | $adsp2100Writes             | $adsp2100Cpi | ${adsp2100Bytes.length} | ${aluDutyCycle("adsp2100", adsp2100Cycles)} | ${memBwEfficiency(16.0, adsp2100Reads, adsp2100Writes, adsp2100Insts)} | ${regPortStress("adsp2100")} |",
      s"| IBM MWave DSP       | 16                | $ibmmwaveCycles              | $ibmmwaveInsts                   | $ibmmwaveReads            | $ibmmwaveWrites             | $ibmmwaveCpi | ${ibmmwaveBytes.length} | ${aluDutyCycle("ibmmwave", ibmmwaveCycles)} | ${memBwEfficiency(16.0, ibmmwaveReads, ibmmwaveWrites, ibmmwaveInsts)} | ${regPortStress("ibmmwave")} |",
      s"| 3dfx Voodoo1        | 32                | $voodoo1Cycles              | $voodoo1Insts                   | $voodoo1Reads            | $voodoo1Writes             | $voodoo1Cpi | ${voodoo1Bytes.length} | ${aluDutyCycle("voodoo1", voodoo1Cycles)} | ${memBwEfficiency(32.0, voodoo1Reads, voodoo1Writes, voodoo1Insts)} | ${regPortStress("voodoo1")} |",
      s"| NVIDIA GeForce 256  | 32                | $geforce256Cycles              | $geforce256Insts                   | $geforce256Reads            | $geforce256Writes             | $geforce256Cpi | ${geforce256Bytes.length} | ${aluDutyCycle("geforce256", geforce256Cycles)} | ${memBwEfficiency(32.0, geforce256Reads, geforce256Writes, geforce256Insts)} | ${regPortStress("geforce256")} |",
      s"| ATI Radeon R100     | 32                | $radeonr100Cycles              | $radeonr100Insts                   | $radeonr100Reads            | $radeonr100Writes             | $radeonr100Cpi | ${radeonr100Bytes.length} | ${aluDutyCycle("radeonr100", radeonr100Cycles)} | ${memBwEfficiency(32.0, radeonr100Reads, radeonr100Writes, radeonr100Insts)} | ${regPortStress("radeonr100")} |",
      s"| PowerVR Series 1    | 32                | $powervr1Cycles              | $powervr1Insts                   | $powervr1Reads            | $powervr1Writes             | $powervr1Cpi | ${powervr1Bytes.length} | ${aluDutyCycle("powervr1", powervr1Cycles)} | ${memBwEfficiency(32.0, powervr1Reads, powervr1Writes, powervr1Insts)} | ${regPortStress("powervr1")} |",
      s"| ARM Mali-200 GPU    | 32                | $mali200Cycles              | $mali200Insts                   | $mali200Reads            | $mali200Writes             | $mali200Cpi | ${mali200Bytes.length} | ${aluDutyCycle("mali200", mali200Cycles)} | ${memBwEfficiency(32.0, mali200Reads, mali200Writes, mali200Insts)} | ${regPortStress("mali200")} |",
      s"| AMD R600 GPU        | 32                | $amdr600Cycles              | $amdr600Insts                   | $amdr600Reads            | $amdr600Writes             | $amdr600Cpi | ${amdr600Bytes.length} | ${aluDutyCycle("amdr600", amdr600Cycles)} | ${memBwEfficiency(32.0, amdr600Reads, amdr600Writes, amdr600Insts)} | ${regPortStress("amdr600")} |",
      s"| AMD Am2901          | 16                | $amd2901Cycles              | $amd2901Insts                   | $amd2901Reads            | $amd2901Writes             | $amd2901Cpi | ${amd2901Bytes.length} | ${aluDutyCycle("amd2901", amd2901Cycles)} | ${memBwEfficiency(16.0, amd2901Reads, amd2901Writes, amd2901Insts)} | ${regPortStress("amd2901")} |",
      s"| Intel 3002          | 16                | $intel3002Cycles              | $intel3002Insts                   | $intel3002Reads            | $intel3002Writes             | $intel3002Cpi | ${intel3002Bytes.length} | ${aluDutyCycle("intel3002", intel3002Cycles)} | ${memBwEfficiency(16.0, intel3002Reads, intel3002Writes, intel3002Insts)} | ${regPortStress("intel3002")} |",
      s"| NS IMP-16           | 16                | $imp16Cycles              | $imp16Insts                   | $imp16Reads            | $imp16Writes             | $imp16Cpi | ${imp16Bytes.length} | ${aluDutyCycle("imp16", imp16Cycles)} | ${memBwEfficiency(16.0, imp16Reads, imp16Writes, imp16Insts)} | ${regPortStress("imp16")} |",
      s"| Motorola MC10800    | 16                | $mc10800Cycles              | $mc10800Insts                   | $mc10800Reads            | $mc10800Writes             | $mc10800Cpi | ${mc10800Bytes.length} | ${aluDutyCycle("mc10800", mc10800Cycles)} | ${memBwEfficiency(16.0, mc10800Reads, mc10800Writes, mc10800Insts)} | ${regPortStress("mc10800")} |",
      s"| ILLIAC IV           | 64 (SIMD)         | $illiac4Cycles              | $illiac4Insts                   | $illiac4Reads            | $illiac4Writes             | $illiac4Cpi | ${illiac4Bytes.length} | ${aluDutyCycle("illiac4", illiac4Cycles)} | ${memBwEfficiency(64.0, illiac4Reads, illiac4Writes, illiac4Insts)} | ${regPortStress("illiac4")} |",
      s"| ICL DAP             | 1 (Bit-Serial)    | $icldapCycles              | $icldapInsts                   | $icldapReads            | $icldapWrites             | $icldapCpi | ${icldapBytes.length} | ${aluDutyCycle("icldap", icldapCycles)} | ${memBwEfficiency(16.0, icldapReads, icldapWrites, icldapInsts)} | ${regPortStress("icldap")} |",
      s"| Goodyear MPP        | 1 (Bit-Serial)    | $goodmppCycles              | $goodmppInsts                   | $goodmppReads            | $goodmppWrites             | $goodmppCpi | ${goodmppBytes.length} | ${aluDutyCycle("goodmpp", goodmppCycles)} | ${memBwEfficiency(16.0, goodmppReads, goodmppWrites, goodmppInsts)} | ${regPortStress("goodmpp")} |",
      s"| Connection Machine  | 1 (Bit-Serial)    | $cm1Cycles              | $cm1Insts                   | $cm1Reads            | $cm1Writes             | $cm1Cpi | ${cm1Bytes.length} | ${aluDutyCycle("cm1", cm1Cycles)} | ${memBwEfficiency(16.0, cm1Reads, cm1Writes, cm1Insts)} | ${regPortStress("cm1")} |",
      s"| IBM MFAST           | 16 (VLIW)         | $ibmmfastCycles              | $ibmmfastInsts                   | $ibmmfastReads            | $ibmmfastWrites             | $ibmmfastCpi | ${ibmmfastBytes.length} | ${aluDutyCycle("ibmmfast", ibmmfastCycles)} | ${memBwEfficiency(16.0, ibmmfastReads, ibmmfastWrites, ibmmfastInsts)} | ${regPortStress("ibmmfast")} |",
      s"| Multiflow TRACE     | 32                | $multiflowCycles              | $multiflowInsts                   | $multiflowReads            | $multiflowWrites             | $multiflowCpi | ${multiflowBytes.length} | ${aluDutyCycle("multiflow", multiflowCycles)} | ${memBwEfficiency(32.0, multiflowReads, multiflowWrites, multiflowInsts)} | ${regPortStress("multiflow")} |",
      s"| Cydrome Cydra 5     | 32                | $cydra5Cycles              | $cydra5Insts                   | $cydra5Reads            | $cydra5Writes             | $cydra5Cpi | ${cydra5Bytes.length} | ${aluDutyCycle("cydra5", cydra5Cycles)} | ${memBwEfficiency(32.0, cydra5Reads, cydra5Writes, cydra5Insts)} | ${regPortStress("cydra5")} |",
      s"| TI TMS320C6000      | 32                | $tms320c6kCycles              | $tms320c6kInsts                   | $tms320c6kReads            | $tms320c6kWrites             | $tms320c6kCpi | ${tms320c6kBytes.length} | ${aluDutyCycle("tms320c6k", tms320c6kCycles)} | ${memBwEfficiency(32.0, tms320c6kReads, tms320c6kWrites, tms320c6kInsts)} | ${regPortStress("tms320c6k")} |",
      s"| Transmeta Crusoe    | 32                | $crusoeCycles              | $crusoeInsts                   | $crusoeReads            | $crusoeWrites             | $crusoeCpi | ${crusoeBytes.length} | ${aluDutyCycle("crusoe", crusoeCycles)} | ${memBwEfficiency(32.0, crusoeReads, crusoeWrites, crusoeInsts)} | ${regPortStress("crusoe")} |",
      s"| Intel Itanium       | 64                | $itaniumCycles              | $itaniumInsts                   | $itaniumReads            | $itaniumWrites             | $itaniumCpi | ${itaniumBytes.length} | ${aluDutyCycle("itanium", itaniumCycles)} | ${memBwEfficiency(64.0, itaniumReads, itaniumWrites, itaniumInsts)} | ${regPortStress("itanium")} |",
      s"| CDC STAR-100        | 32                | $cdcstar100Cycles              | $cdcstar100Insts                   | $cdcstar100Reads            | $cdcstar100Writes             | $cdcstar100Cpi | ${cdcstar100Bytes.length} | ${aluDutyCycle("cdcstar100", cdcstar100Cycles)} | ${memBwEfficiency(32.0, cdcstar100Reads, cdcstar100Writes, cdcstar100Insts)} | ${regPortStress("cdcstar100")} |",
      s"| TI ASC              | 32                | $tiascCycles              | $tiascInsts                   | $tiascReads            | $tiascWrites             | $tiascCpi | ${tiascBytes.length} | ${aluDutyCycle("tiasc", tiascCycles)} | ${memBwEfficiency(32.0, tiascReads, tiascWrites, tiascInsts)} | ${regPortStress("tiasc")} |",
      s"| Convex C1           | 32                | $convexc1Cycles              | $convexc1Insts                   | $convexc1Reads            | $convexc1Writes             | $convexc1Cpi | ${convexc1Bytes.length} | ${aluDutyCycle("convexc1", convexc1Cycles)} | ${memBwEfficiency(32.0, convexc1Reads, convexc1Writes, convexc1Insts)} | ${regPortStress("convexc1")} |",
      s"| NEC SX-2            | 32                | $necsx2Cycles              | $necsx2Insts                   | $necsx2Reads            | $necsx2Writes             | $necsx2Cpi | ${necsx2Bytes.length} | ${aluDutyCycle("necsx2", necsx2Cycles)} | ${memBwEfficiency(32.0, necsx2Reads, necsx2Writes, necsx2Insts)} | ${regPortStress("necsx2")} |",
      s"| IBM S/370 VF        | 32                | $ibms370vfCycles              | $ibms370vfInsts                   | $ibms370vfReads            | $ibms370vfWrites             | $ibms370vfCpi | ${ibms370vfBytes.length} | ${aluDutyCycle("ibms370vf", ibms370vfCycles)} | ${memBwEfficiency(32.0, ibms370vfReads, ibms370vfWrites, ibms370vfInsts)} | ${regPortStress("ibms370vf")} |",
      s"| IBM 801             | 32                | $ibm801Cycles              | $ibm801Insts                   | $ibm801Reads            | $ibm801Writes             | $ibm801Cpi | ${ibm801Bytes.length} | ${aluDutyCycle("ibm801", ibm801Cycles)} | ${memBwEfficiency(32.0, ibm801Reads, ibm801Writes, ibm801Insts)} | ${regPortStress("ibm801")} |",
      s"| SPARC               | 32                | $sparcCycles              | $sparcInsts                   | $sparcReads            | $sparcWrites             | $sparcCpi | ${sparcBytes.length} | ${aluDutyCycle("sparc", sparcCycles)} | ${memBwEfficiency(32.0, sparcReads, sparcWrites, sparcInsts)} | ${regPortStress("sparc")} |",
      s"| PowerPC             | 32                | $powerpcCycles              | $powerpcInsts                   | $powerpcReads            | $powerpcWrites             | $powerpcCpi | ${powerpcBytes.length} | ${aluDutyCycle("powerpc", powerpcCycles)} | ${memBwEfficiency(32.0, powerpcReads, powerpcWrites, powerpcInsts)} | ${regPortStress("powerpc")} |",
      s"| JVM                 | 32                | $jvmCycles              | $jvmInsts                   | $jvmReads            | $jvmWrites             | $jvmCpi | ${jvmBytes.length} | ${aluDutyCycle("jvm", jvmCycles)} | ${memBwEfficiency(32.0, jvmReads, jvmWrites, jvmInsts)} | ${regPortStress("jvm")} |",
      s"| Setun               | 32                | $setunCycles              | $setunInsts                   | $setunReads            | $setunWrites             | $setunCpi | ${setunBytes.length} | ${aluDutyCycle("setun", setunCycles)} | ${memBwEfficiency(32.0, setunReads, setunWrites, setunInsts)} | ${regPortStress("setun")} |",
      s"| IBM 1620            | 32                | $ibm1620Cycles            | $ibm1620Insts                 | $ibm1620Reads          | $ibm1620Writes          | $ibm1620Cpi | ${ibm1620Bytes.length} | ${aluDutyCycle("ibm1620", ibm1620Cycles)} | ${memBwEfficiency(32.0, ibm1620Reads, ibm1620Writes, ibm1620Insts)} | ${regPortStress("ibm1620")} |",
      s"| Symbolics 3600      | 32                | $symbolicsCycles          | $symbolicsInsts               | $symbolicsReads        | $symbolicsWrites        | $symbolicsCpi | ${symbolicsBytes.length} | ${aluDutyCycle("symbolics3600", symbolicsCycles)} | ${memBwEfficiency(32.0, symbolicsReads, symbolicsWrites, symbolicsInsts)} | ${regPortStress("symbolics3600")} |",
      s"| MIT Dataflow        | 32                | $mitdataflowCycles        | $mitdataflowInsts             | $mitdataflowReads      | $mitdataflowWrites      | $mitdataflowCpi | ${mitdataflowBytes.length} | ${aluDutyCycle("mitdataflow", mitdataflowCycles)} | ${memBwEfficiency(32.0, mitdataflowReads, mitdataflowWrites, mitdataflowInsts)} | ${regPortStress("mitdataflow")} |",
      s"| SUBLEQ              | 32                | $subleqCycles             | $subleqInsts                  | $subleqReads           | $subleqWrites           | $subleqCpi | ${subleqBytes.length} | ${aluDutyCycle("subleq", subleqCycles)} | ${memBwEfficiency(32.0, subleqReads, subleqWrites, subleqInsts)} | ${regPortStress("subleq")} |",
      s"| SOAR                | 32                | $soarCycles               | $soarInsts                    | $soarReads             | $soarWrites             | $soarCpi | ${soarBytes.length} | ${aluDutyCycle("soar", soarCycles)} | ${memBwEfficiency(32.0, soarReads, soarWrites, soarInsts)} | ${regPortStress("soar")} |",
      s"| TTA                 | 32                | $ttaCycles                | $ttaInsts                     | $ttaReads              | $ttaWrites              | $ttaCpi | ${ttaBytes.length} | ${aluDutyCycle("tta", ttaCycles)} | ${memBwEfficiency(32.0, ttaReads, ttaWrites, ttaInsts)} | ${regPortStress("tta")} |"
    )

    val sortedRows = rows.sortBy(row => row.split('|')(1).trim.toLowerCase)
    val table = s"""
| Target Architecture | Word Width (bits) | Execution Cycles | Retired Instructions | Memory Reads | Memory Writes | CPI | Code Footprint (words) | ALU Duty Cycle | Mem BW Efficiency | Register Port Stress |
|---------------------|-------------------|------------------|----------------------|--------------|---------------|-----|------------------------|----------------|-------------------|----------------------|
${sortedRows.mkString("\n")}
"""

    println("\n=== COMPARATIVE ARCHITECTURE PERFORMANCE REPORT ===")
    println(table)
    println("===================================================\n")

    // Save report directly to the bottom of README.md
    val readmeFile = findWorkspaceFile("README.md")
    if (readmeFile.exists()) {
      val lines = scala.io.Source.fromFile(readmeFile).getLines().toList
      val targetHeader = "## 📊 Architecture Comparison Report"
      val index = lines.indexWhere(_.trim.startsWith(targetHeader))
      val prefix = if (index >= 0) lines.take(index) else lines
      
      val writer = new java.io.PrintWriter(readmeFile)
      prefix.foreach(l => writer.write(l + "\n"))
      if (prefix.lastOption.exists(_.trim.nonEmpty)) {
        writer.write("\n")
      }
      writer.write(targetHeader + "\n\n")
      writer.write(table)
      writer.close()
    }
  }
}
