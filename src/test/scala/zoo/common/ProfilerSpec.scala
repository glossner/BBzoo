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
import zoo.manchester.ManchesterCore
import zoo.univac1.Univac1Core
import zoo.ias.IasCore
import zoo.edsac.EdsacCore
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

  it should "profile and compare execution statistics for all 23 cores" in {
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
    val manchesterHex = findWorkspaceFile("manchester/sw/test_vector.hex")
    val manchesterBytes = Source.fromFile(manchesterHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseLong(l.trim, 16).toInt).toArray
    var manchesterCycles = 0L
    var manchesterInsts = 0L
    var manchesterReads = 0L
    var manchesterWrites = 0L

    simulate(new ManchesterCore) { c =>
      val mem = Array.fill(256)(0)
      for (i <- manchesterBytes.indices) mem(i) = manchesterBytes(i)
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
      manchesterCycles = c.io.pmu_cycles.peek().litValue.toLong
      manchesterInsts  = c.io.pmu_insts.peek().litValue.toLong
      manchesterReads  = c.io.pmu_reads.peek().litValue.toLong
      manchesterWrites = c.io.pmu_writes.peek().litValue.toLong

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
    val iasHex = findWorkspaceFile("ias/sw/test_vector.hex")
    val iasBytes = Source.fromFile(iasHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => java.lang.Long.parseUnsignedLong(l.trim, 16)).toArray
    var iasCycles = 0L
    var iasInsts = 0L
    var iasReads = 0L
    var iasWrites = 0L

    simulate(new IasCore) { c =>
      val mem = Array.fill(4096)(0L)
      for (i <- iasBytes.indices) mem(i) = iasBytes(i)
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
      iasCycles = c.io.pmu_cycles.peek().litValue.toLong
      iasInsts  = c.io.pmu_insts.peek().litValue.toLong
      iasReads  = c.io.pmu_reads.peek().litValue.toLong
      iasWrites = c.io.pmu_writes.peek().litValue.toLong

      mem(48) shouldBe 11L
      mem(49) shouldBe 22L
      mem(50) shouldBe 33L
      mem(51) shouldBe 44L
    }

    // 15. EDSAC
    val edsacHex = findWorkspaceFile("edsac/sw/test_vector.hex")
    val edsacBytes = Source.fromFile(edsacHex).getLines().filterNot(l => l.trim.isEmpty || l.trim.startsWith("#")).map(l => Integer.parseInt(l.trim, 16)).toArray
    var edsacCycles = 0L
    var edsacInsts = 0L
    var edsacReads = 0L
    var edsacWrites = 0L

    simulate(new EdsacCore) { c =>
      val mem = Array.fill(1024)(0)
      for (i <- edsacBytes.indices) mem(i) = edsacBytes(i)
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
      edsacCycles = c.io.pmu_cycles.peek().litValue.toLong
      edsacInsts  = c.io.pmu_insts.peek().litValue.toLong
      edsacReads  = c.io.pmu_reads.peek().litValue.toLong
      edsacWrites = c.io.pmu_writes.peek().litValue.toLong

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

    // Print Consolidated Comparative Table
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
    val manchesterCpi = if (manchesterInsts > 0) String.format("%.2f", Double.box(manchesterCycles.toDouble / manchesterInsts)) else "N/A"
    val univacCpi  = if (univacInsts > 0)  String.format("%.2f", Double.box(univacCycles.toDouble / univacInsts))   else "N/A"
    val iasCpi     = if (iasInsts > 0)     String.format("%.2f", Double.box(iasCycles.toDouble / iasInsts))       else "N/A"
    val edsacCpi   = if (edsacInsts > 0)   String.format("%.2f", Double.box(edsacCycles.toDouble / edsacInsts))   else "N/A"
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

    val table = s"""
| Target Architecture | Word Width (bits) | Execution Cycles | Retired Instructions | Memory Reads | Memory Writes | CPI |
|---------------------|-------------------|------------------|----------------------|--------------|---------------|-----|
| Babbage Anal. Eng.  | 64                | $babbageCycles              | $babbageInsts                   | $babbageReads            | $babbageWrites             | $babbageCpi |
| Harvard Mark I      | 64                | $harvardCycles              | $harvardInsts                   | $harvardReads            | $harvardWrites             | $harvardCpi |
| Zuse Z1             | 22                | $zuseCycles              | $zuseInsts                   | $zuseReads            | $zuseWrites             | $zuseCpi |
| Manchester Baby     | 32                | $manchesterCycles              | $manchesterInsts                   | $manchesterReads            | $manchesterWrites             | $manchesterCpi |
| Univac I            | 72                | $univacCycles              | $univacInsts                   | $univacReads            | $univacWrites             | $univacCpi |
| Princeton IAS       | 40                | $iasCycles              | $iasInsts                   | $iasReads            | $iasWrites             | $iasCpi |
| EDSAC               | 17                | $edsacCycles              | $edsacInsts                   | $edsacReads            | $edsacWrites             | $edsacCpi |
| IBM 701             | 36                | $ibm701Cycles              | $ibm701Insts                   | $ibm701Reads            | $ibm701Writes             | $ibm701Cpi |
| IBM 704             | 36                | $ibm704Cycles              | $ibm704Insts                   | $ibm704Reads            | $ibm704Writes             | $ibm704Cpi |
| IBM 650             | 40                | $ibm650Cycles              | $ibm650Insts                   | $ibm650Reads            | $ibm650Writes             | $ibm650Cpi |
| IBM 705             | 35                | $ibm705Cycles              | $ibm705Insts                   | $ibm705Reads            | $ibm705Writes             | $ibm705Cpi |
| IBM 1401            | 36                | $ibm1401Cycles              | $ibm1401Insts                   | $ibm1401Reads            | $ibm1401Writes             | $ibm1401Cpi |
| STC ZEBRA           | 33                | $stczebraCycles              | $stczebraInsts                   | $stczebraReads            | $stczebraWrites             | $stczebraCpi |
| Bull Gamma 60        | 24                | $bullgammaCycles              | $bullgammaInsts                   | $bullgammaReads            | $bullgammaWrites             | $bullgammaCpi |
| IBM Stretch         | 64                | $ibmstretchCycles              | $ibmstretchInsts                   | $ibmstretchReads            | $ibmstretchWrites             | $ibmstretchCpi |
| MOS 6502            | 8                 | $mosCycles              | $mosInsts                   | $mosReads            | $mosWrites             | $mosCpi |
| DEC PDP-8           | 12                | $pdp8Cycles              | $pdp8Insts                   | $pdp8Reads            | $pdp8Writes             | $pdp8Cpi |
| DEC PDP-11          | 16                | $pdp11Cycles              | $pdp11Insts                   | $pdp11Reads            | $pdp11Writes             | $pdp11Cpi |
| IBM System/360      | 32                | $ibmCycles              | $ibmInsts                   | $ibmReads            | $ibmWrites             | $ibmCpi |
| Motorola 68000      | 32                | $m68kCycles              | $m68kInsts                   | $m68kReads            | $m68kWrites             | $m68kCpi |
| Burroughs B5500     | 48                | $b5500Cycles              | $b5500Insts                   | $b5500Reads            | $b5500Writes             | $b5500Cpi |
| CDC 6600            | 60                | $cdcCycles              | $cdcInsts                   | $cdcReads            | $cdcWrites             | $cdcCpi |
| Cray-1              | 64 (Vector)       | $crayCycles              | $crayInsts                   | $crayReads            | $crayWrites             | $crayCpi |
| Univac 1103A        | 36                | $univac1103aCycles              | $univac1103aInsts                   | $univac1103aReads            | $univac1103aWrites             | $univac1103aCpi |
| CDC 6600 PPU        | 12                | $cdc6600ppuCycles              | $cdc6600ppuInsts                   | $cdc6600ppuReads            | $cdc6600ppuWrites             | $cdc6600ppuCpi |
| DEC VAX             | 32                | $decvaxCycles              | $decvaxInsts                   | $decvaxReads            | $decvaxWrites             | $decvaxCpi |
| Intel 8080A         | 8                 | $intel8080aCycles              | $intel8080aInsts                   | $intel8080aReads            | $intel8080aWrites             | $intel8080aCpi |
| Motorola 6800       | 8                 | $motorola6800Cycles              | $motorola6800Insts                   | $motorola6800Reads            | $motorola6800Writes             | $motorola6800Cpi |
| IBM 6150 ROMP       | 32                | $ibm6150Cycles              | $ibm6150Insts                   | $ibm6150Reads            | $ibm6150Writes             | $ibm6150Cpi |
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
