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

  it should "profile and compare execution statistics for all 39 cores" in {
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

    // Print Consolidated Comparative Table
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
        case "mips1" | "arm1" | "berkeleyrisc" | "ibm6150" | "hp3000" | "ethlilith" => 8.0
        case "b5500" | "ucsdp" => 6.0
        case "mos" | "intel8080a" | "motorola6800" | "pdp8" | "upd7720" | "tms32010" | "adsp2100" | "ibmmwave" => 4.0
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
        case "mips1" | "arm1" | "berkeleyrisc" | "ibm6150" | "decvax" | "pdp11" | "m68k" => 2.5
        case "mos" | "intel8080a" | "motorola6800" | "pdp8" | "upd7720" | "tms32010" | "adsp2100" | "ibmmwave" => 1.2
        case "b5500" | "ucsdp" | "hp3000" | "ethlilith" | "cambridgeedsac" | "manchestermu1" | "babbage" | "harvard" | "zuse" | "univac" | "princetonias" => 0.5
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

    val table = s"""
| Target Architecture | Word Width (bits) | Execution Cycles | Retired Instructions | Memory Reads | Memory Writes | CPI | Code Footprint (words) | ALU Duty Cycle | Mem BW Efficiency | Register Port Stress |
|---------------------|-------------------|------------------|----------------------|--------------|---------------|-----|------------------------|----------------|-------------------|----------------------|
| Babbage Anal. Eng.  | 64                | $babbageCycles              | $babbageInsts                   | $babbageReads            | $babbageWrites             | $babbageCpi | ${babbageBytes.length} | ${aluDutyCycle("babbage", babbageCycles)} | ${memBwEfficiency(64.0, babbageReads, babbageWrites, babbageInsts)} | ${regPortStress("babbage")} |
| Harvard Mark I      | 64                | $harvardCycles              | $harvardInsts                   | $harvardReads            | $harvardWrites             | $harvardCpi | ${harvardBytes.length} | ${aluDutyCycle("harvard", harvardCycles)} | ${memBwEfficiency(64.0, harvardReads, harvardWrites, harvardInsts)} | ${regPortStress("harvard")} |
| Zuse Z1             | 22                | $zuseCycles              | $zuseInsts                   | $zuseReads            | $zuseWrites             | $zuseCpi | ${zuseBytes.length} | ${aluDutyCycle("zuse", zuseCycles)} | ${memBwEfficiency(22.0, zuseReads, zuseWrites, zuseInsts)} | ${regPortStress("zuse")} |
| Manchester Baby     | 32                | $manchestermu1Cycles              | $manchestermu1Insts                   | $manchestermu1Reads            | $manchestermu1Writes             | $manchestermu1Cpi | ${manchestermu1Bytes.length} | ${aluDutyCycle("manchestermu1", manchestermu1Cycles)} | ${memBwEfficiency(32.0, manchestermu1Reads, manchestermu1Writes, manchestermu1Insts)} | ${regPortStress("manchestermu1")} |
| Univac I            | 72                | $univacCycles              | $univacInsts                   | $univacReads            | $univacWrites             | $univacCpi | ${univacBytes.length} | ${aluDutyCycle("univac", univacCycles)} | ${memBwEfficiency(72.0, univacReads, univacWrites, univacInsts)} | ${regPortStress("univac")} |
| Princeton IAS       | 40                | $princetoniasCycles              | $princetoniasInsts                   | $princetoniasReads            | $princetoniasWrites             | $princetoniasCpi | ${princetoniasBytes.length} | ${aluDutyCycle("princetonias", princetoniasCycles)} | ${memBwEfficiency(40.0, princetoniasReads, princetoniasWrites, princetoniasInsts)} | ${regPortStress("princetonias")} |
| EDSAC               | 17                | $cambridgeedsacCycles              | $cambridgeedsacInsts                   | $cambridgeedsacReads            | $cambridgeedsacWrites             | $cambridgeedsacCpi | ${cambridgeedsacBytes.length} | ${aluDutyCycle("cambridgeedsac", cambridgeedsacCycles)} | ${memBwEfficiency(17.0, cambridgeedsacReads, cambridgeedsacWrites, cambridgeedsacInsts)} | ${regPortStress("cambridgeedsac")} |
| IBM 701             | 36                | $ibm701Cycles              | $ibm701Insts                   | $ibm701Reads            | $ibm701Writes             | $ibm701Cpi | ${ibm701Bytes.length} | ${aluDutyCycle("ibm701", ibm701Cycles)} | ${memBwEfficiency(36.0, ibm701Reads, ibm701Writes, ibm701Insts)} | ${regPortStress("ibm701")} |
| IBM 704             | 36                | $ibm704Cycles              | $ibm704Insts                   | $ibm704Reads            | $ibm704Writes             | $ibm704Cpi | ${ibm704Bytes.length} | ${aluDutyCycle("ibm704", ibm704Cycles)} | ${memBwEfficiency(36.0, ibm704Reads, ibm704Writes, ibm704Insts)} | ${regPortStress("ibm704")} |
| IBM 650             | 40                | $ibm650Cycles              | $ibm650Insts                   | $ibm650Reads            | $ibm650Writes             | $ibm650Cpi | ${ibm650Bytes.length} | ${aluDutyCycle("ibm650", ibm650Cycles)} | ${memBwEfficiency(40.0, ibm650Reads, ibm650Writes, ibm650Insts)} | ${regPortStress("ibm650")} |
| IBM 705             | 35                | $ibm705Cycles              | $ibm705Insts                   | $ibm705Reads            | $ibm705Writes             | $ibm705Cpi | ${ibm705Bytes.length} | ${aluDutyCycle("ibm705", ibm705Cycles)} | ${memBwEfficiency(35.0, ibm705Reads, ibm705Writes, ibm705Insts)} | ${regPortStress("ibm705")} |
| IBM 1401            | 36                | $ibm1401Cycles              | $ibm1401Insts                   | $ibm1401Reads            | $ibm1401Writes             | $ibm1401Cpi | ${ibm1401Bytes.length} | ${aluDutyCycle("ibm1401", ibm1401Cycles)} | ${memBwEfficiency(36.0, ibm1401Reads, ibm1401Writes, ibm1401Insts)} | ${regPortStress("ibm1401")} |
| STC ZEBRA           | 33                | $stczebraCycles              | $stczebraInsts                   | $stczebraReads            | $stczebraWrites             | $stczebraCpi | ${stczebraBytes.length} | ${aluDutyCycle("stczebra", stczebraCycles)} | ${memBwEfficiency(33.0, stczebraReads, stczebraWrites, stczebraInsts)} | ${regPortStress("stczebra")} |
| Bull Gamma 60        | 24                | $bullgammaCycles              | $bullgammaInsts                   | $bullgammaReads            | $bullgammaWrites             | $bullgammaCpi | ${bullgammaBytes.length} | ${aluDutyCycle("bullgamma", bullgammaCycles)} | ${memBwEfficiency(24.0, bullgammaReads, bullgammaWrites, bullgammaInsts)} | ${regPortStress("bullgamma")} |
| IBM Stretch         | 64                | $ibmstretchCycles              | $ibmstretchInsts                   | $ibmstretchReads            | $ibmstretchWrites             | $ibmstretchCpi | ${ibmstretchBytes.length} | ${aluDutyCycle("ibmstretch", ibmstretchCycles)} | ${memBwEfficiency(64.0, ibmstretchReads, ibmstretchWrites, ibmstretchInsts)} | ${regPortStress("ibmstretch")} |
| MOS 6502            | 8                 | $mosCycles              | $mosInsts                   | $mosReads            | $mosWrites             | $mosCpi | ${mosBytes.length} | ${aluDutyCycle("mos", mosCycles)} | ${memBwEfficiency(8.0, mosReads, mosWrites, mosInsts)} | ${regPortStress("mos")} |
| DEC PDP-8           | 12                | $pdp8Cycles              | $pdp8Insts                   | $pdp8Reads            | $pdp8Writes             | $pdp8Cpi | ${pdp8Bytes.length} | ${aluDutyCycle("pdp8", pdp8Cycles)} | ${memBwEfficiency(12.0, pdp8Reads, pdp8Writes, pdp8Insts)} | ${regPortStress("pdp8")} |
| DEC PDP-11          | 16                | $pdp11Cycles              | $pdp11Insts                   | $pdp11Reads            | $pdp11Writes             | $pdp11Cpi | ${pdp11Bytes.length} | ${aluDutyCycle("pdp11", pdp11Cycles)} | ${memBwEfficiency(16.0, pdp11Reads, pdp11Writes, pdp11Insts)} | ${regPortStress("pdp11")} |
| IBM System/360      | 32                | $ibmCycles              | $ibmInsts                   | $ibmReads            | $ibmWrites             | $ibmCpi | ${ibmBytes.length} | ${aluDutyCycle("ibm360", ibmCycles)} | ${memBwEfficiency(32.0, ibmReads, ibmWrites, ibmInsts)} | ${regPortStress("ibm360")} |
| Motorola 68000      | 32                | $m68kCycles              | $m68kInsts                   | $m68kReads            | $m68kWrites             | $m68kCpi | ${m68kBytes.length} | ${aluDutyCycle("m68k", m68kCycles)} | ${memBwEfficiency(32.0, m68kReads, m68kWrites, m68kInsts)} | ${regPortStress("m68k")} |
| Burroughs B5500     | 48                | $b5500Cycles              | $b5500Insts                   | $b5500Reads            | $b5500Writes             | $b5500Cpi | ${b5500Bytes.length} | ${aluDutyCycle("b5500", b5500Cycles)} | ${memBwEfficiency(48.0, b5500Reads, b5500Writes, b5500Insts)} | ${regPortStress("b5500")} |
| CDC 6600            | 60                | $cdcCycles              | $cdcInsts                   | $cdcReads            | $cdcWrites             | $cdcCpi | ${cdcBytes.length} | ${aluDutyCycle("cdc", cdcCycles)} | ${memBwEfficiency(60.0, cdcReads, cdcWrites, cdcInsts)} | ${regPortStress("cdc")} |
| Cray-1              | 64 (Vector)       | $crayCycles              | $crayInsts                   | $crayReads            | $crayWrites             | $crayCpi | ${crayBytes.length} | ${aluDutyCycle("cray", crayCycles)} | ${memBwEfficiency(64.0, crayReads, crayWrites, crayInsts)} | ${regPortStress("cray")} |
| Univac 1103A        | 36                | $univac1103aCycles              | $univac1103aInsts                   | $univac1103aReads            | $univac1103aWrites             | $univac1103aCpi | ${univac1103aBytes.length} | ${aluDutyCycle("univac1103a", univac1103aCycles)} | ${memBwEfficiency(36.0, univac1103aReads, univac1103aWrites, univac1103aInsts)} | ${regPortStress("univac1103a")} |
| CDC 6600 PPU        | 12                | $cdc6600ppuCycles              | $cdc6600ppuInsts                   | $cdc6600ppuReads            | $cdc6600ppuWrites             | $cdc6600ppuCpi | ${cdc6600ppuBytes.length} | ${aluDutyCycle("cdc6600ppu", cdc6600ppuCycles)} | ${memBwEfficiency(12.0, cdc6600ppuReads, cdc6600ppuWrites, cdc6600ppuInsts)} | ${regPortStress("cdc6600ppu")} |
| DEC VAX             | 32                | $decvaxCycles              | $decvaxInsts                   | $decvaxReads            | $decvaxWrites             | $decvaxCpi | ${decvaxBytes.length} | ${aluDutyCycle("decvax", decvaxCycles)} | ${memBwEfficiency(32.0, decvaxReads, decvaxWrites, decvaxInsts)} | ${regPortStress("decvax")} |
| Intel 8080A         | 8                 | $intel8080aCycles              | $intel8080aInsts                   | $intel8080aReads            | $intel8080aWrites             | $intel8080aCpi | ${intel8080aBytes.length} | ${aluDutyCycle("intel8080a", intel8080aCycles)} | ${memBwEfficiency(8.0, intel8080aReads, intel8080aWrites, intel8080aInsts)} | ${regPortStress("intel8080a")} |
| Motorola 6800       | 8                 | $motorola6800Cycles              | $motorola6800Insts                   | $motorola6800Reads            | $motorola6800Writes             | $motorola6800Cpi | ${motorola6800Bytes.length} | ${aluDutyCycle("motorola6800", motorola6800Cycles)} | ${memBwEfficiency(8.0, motorola6800Reads, motorola6800Writes, motorola6800Insts)} | ${regPortStress("motorola6800")} |
| IBM 6150 ROMP       | 32                | $ibm6150Cycles              | $ibm6150Insts                   | $ibm6150Reads            | $ibm6150Writes             | $ibm6150Cpi | ${ibm6150Bytes.length} | ${aluDutyCycle("ibm6150", ibm6150Cycles)} | ${memBwEfficiency(32.0, ibm6150Reads, ibm6150Writes, ibm6150Insts)} | ${regPortStress("ibm6150")} |
| MIPS I (R2000)      | 32                | $mips1Cycles              | $mips1Insts                   | $mips1Reads            | $mips1Writes             | $mips1Cpi | ${mips1Bytes.length} | ${aluDutyCycle("mips1", mips1Cycles)} | ${memBwEfficiency(32.0, mips1Reads, mips1Writes, mips1Insts)} | ${regPortStress("mips1")} |
| ARM1                | 32                | $arm1Cycles              | $arm1Insts                   | $arm1Reads            | $arm1Writes             | $arm1Cpi | ${arm1Bytes.length} | ${aluDutyCycle("arm1", arm1Cycles)} | ${memBwEfficiency(32.0, arm1Reads, arm1Writes, arm1Insts)} | ${regPortStress("arm1")} |
| Berkeley RISC-I     | 32                | $berkeleyriscCycles              | $berkeleyriscInsts                   | $berkeleyriscReads            | $berkeleyriscWrites             | $berkeleyriscCpi | ${berkeleyriscBytes.length} | ${aluDutyCycle("berkeleyrisc", berkeleyriscCycles)} | ${memBwEfficiency(32.0, berkeleyriscReads, berkeleyriscWrites, berkeleyriscInsts)} | ${regPortStress("berkeleyrisc")} |
| HP 3000             | 16                | $hp3000Cycles              | $hp3000Insts                   | $hp3000Reads            | $hp3000Writes             | $hp3000Cpi | ${hp3000Bytes.length} | ${aluDutyCycle("hp3000", hp3000Cycles)} | ${memBwEfficiency(16.0, hp3000Reads, hp3000Writes, hp3000Insts)} | ${regPortStress("hp3000")} |
| Ethlilith           | 16                | $ethlilithCycles              | $ethlilithInsts                   | $ethlilithReads            | $ethlilithWrites             | $ethlilithCpi | ${ethlilithBytes.length} | ${aluDutyCycle("ethlilith", ethlilithCycles)} | ${memBwEfficiency(16.0, ethlilithReads, ethlilithWrites, ethlilithInsts)} | ${regPortStress("ethlilith")} |
| UCSD Pascal P-Mach  | 16                | $ucsdpCycles              | $ucsdpInsts                   | $ucsdpReads            | $ucsdpWrites             | $ucsdpCpi | ${ucsdpBytes.length} | ${aluDutyCycle("ucsdp", ucsdpCycles)} | ${memBwEfficiency(16.0, ucsdpReads, ucsdpWrites, ucsdpInsts)} | ${regPortStress("ucsdp")} |
| NEC uPD7720 DSP     | 16                | $upd7720Cycles              | $upd7720Insts                   | $upd7720Reads            | $upd7720Writes             | $upd7720Cpi | ${upd7720Bytes.length} | ${aluDutyCycle("upd7720", upd7720Cycles)} | ${memBwEfficiency(16.0, upd7720Reads, upd7720Writes, upd7720Insts)} | ${regPortStress("upd7720")} |
| TI TMS32010 DSP     | 16                | $tms32010Cycles              | $tms32010Insts                   | $tms32010Reads            | $tms32010Writes             | $tms32010Cpi | ${tms32010Bytes.length} | ${aluDutyCycle("tms32010", tms32010Cycles)} | ${memBwEfficiency(16.0, tms32010Reads, tms32010Writes, tms32010Insts)} | ${regPortStress("tms32010")} |
| ADI ADSP-2100 DSP   | 16                | $adsp2100Cycles              | $adsp2100Insts                   | $adsp2100Reads            | $adsp2100Writes             | $adsp2100Cpi | ${adsp2100Bytes.length} | ${aluDutyCycle("adsp2100", adsp2100Cycles)} | ${memBwEfficiency(16.0, adsp2100Reads, adsp2100Writes, adsp2100Insts)} | ${regPortStress("adsp2100")} |
| IBM MWave DSP       | 16                | $ibmmwaveCycles              | $ibmmwaveInsts                   | $ibmmwaveReads            | $ibmmwaveWrites             | $ibmmwaveCpi | ${ibmmwaveBytes.length} | ${aluDutyCycle("ibmmwave", ibmmwaveCycles)} | ${memBwEfficiency(16.0, ibmmwaveReads, ibmmwaveWrites, ibmmwaveInsts)} | ${regPortStress("ibmmwave")} |
| 3dfx Voodoo1        | 32                | $voodoo1Cycles              | $voodoo1Insts                   | $voodoo1Reads            | $voodoo1Writes             | $voodoo1Cpi | ${voodoo1Bytes.length} | ${aluDutyCycle("voodoo1", voodoo1Cycles)} | ${memBwEfficiency(32.0, voodoo1Reads, voodoo1Writes, voodoo1Insts)} | ${regPortStress("voodoo1")} |
| NVIDIA GeForce 256  | 32                | $geforce256Cycles              | $geforce256Insts                   | $geforce256Reads            | $geforce256Writes             | $geforce256Cpi | ${geforce256Bytes.length} | ${aluDutyCycle("geforce256", geforce256Cycles)} | ${memBwEfficiency(32.0, geforce256Reads, geforce256Writes, geforce256Insts)} | ${regPortStress("geforce256")} |
| ATI Radeon R100     | 32                | $radeonr100Cycles              | $radeonr100Insts                   | $radeonr100Reads            | $radeonr100Writes             | $radeonr100Cpi | ${radeonr100Bytes.length} | ${aluDutyCycle("radeonr100", radeonr100Cycles)} | ${memBwEfficiency(32.0, radeonr100Reads, radeonr100Writes, radeonr100Insts)} | ${regPortStress("radeonr100")} |
| PowerVR Series 1    | 32                | $powervr1Cycles              | $powervr1Insts                   | $powervr1Reads            | $powervr1Writes             | $powervr1Cpi | ${powervr1Bytes.length} | ${aluDutyCycle("powervr1", powervr1Cycles)} | ${memBwEfficiency(32.0, powervr1Reads, powervr1Writes, powervr1Insts)} | ${regPortStress("powervr1")} |
| ARM Mali-200 GPU    | 32                | $mali200Cycles              | $mali200Insts                   | $mali200Reads            | $mali200Writes             | $mali200Cpi | ${mali200Bytes.length} | ${aluDutyCycle("mali200", mali200Cycles)} | ${memBwEfficiency(32.0, mali200Reads, mali200Writes, mali200Insts)} | ${regPortStress("mali200")} |
| AMD R600 GPU        | 32                | $amdr600Cycles              | $amdr600Insts                   | $amdr600Reads            | $amdr600Writes             | $amdr600Cpi | ${amdr600Bytes.length} | ${aluDutyCycle("amdr600", amdr600Cycles)} | ${memBwEfficiency(32.0, amdr600Reads, amdr600Writes, amdr600Insts)} | ${regPortStress("amdr600")} |
"""

    println("\n=== COMPARATIVE ARCHITECTURE PERFORMANCE REPORT ===")
    println(table)
    println("===================================================\n")

    // Save report to file for the user
    val reportFile = findWorkspaceFile("pmu_report.md")
    val writer = new java.io.PrintWriter(reportFile)
    writer.write("# Architecture Comparison Report\n")
    writer.write(table)
    writer.close()
  }
}
