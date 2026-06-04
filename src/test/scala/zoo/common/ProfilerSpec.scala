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

class ProfilerSpec extends AnyFlatSpec with Matchers {
  behavior of "ZooArchitectureProfiler"

  // Helper to find files relative to workspace root from fork directory
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

  it should "profile and compare execution statistics for all cores" in {
    println("\n=== RUNNING BENCHMARKS & GATHERING PMU STATS ===")

    // 1. Profile DEC PDP-8
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

      // Verify results
      mem(22) shouldBe 11
      mem(23) shouldBe 22
      mem(24) shouldBe 33
      mem(25) shouldBe 44
    }

    // 2. Profile IBM System/360
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

      // Verify results
      readWord(84) shouldBe 11
      readWord(88) shouldBe 22
      readWord(92) shouldBe 33
      readWord(96) shouldBe 44
    }

    // 3. Profile Cray-1
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

      // Verify results
      mem(24) shouldBe 11L
      mem(25) shouldBe 22L
      mem(26) shouldBe 33L
      mem(27) shouldBe 44L
    }

    // 4. Profile Motorola 68000
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
        
        // Detect loop or halt condition
        val pc = c.io.debug_pc.peek().litValue
        if (pc == 104) {
          done = true
        }
      }
      m68kCycles = c.io.pmu_cycles.peek().litValue.toLong
      m68kInsts  = c.io.pmu_insts.peek().litValue.toLong
      m68kReads  = c.io.pmu_reads.peek().litValue.toLong
      m68kWrites = c.io.pmu_writes.peek().litValue.toLong

      // Verify results
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

    // Print Consolidated Comparative Table
    val pdp8Cpi = if (pdp8Insts > 0) String.format("%.2f", Double.box(pdp8Cycles.toDouble / pdp8Insts)) else "N/A"
    val ibmCpi  = if (ibmInsts > 0)  String.format("%.2f", Double.box(ibmCycles.toDouble / ibmInsts))   else "N/A"
    val crayCpi = if (crayInsts > 0) String.format("%.2f", Double.box(crayCycles.toDouble / crayInsts)) else "N/A"
    val m68kCpi = if (m68kInsts > 0) String.format("%.2f", Double.box(m68kCycles.toDouble / m68kInsts)) else "N/A"

    val table = s"""
| Target Architecture | Word Width (bits) | Execution Cycles | Retired Instructions | Memory Reads | Memory Writes | CPI |
|---------------------|-------------------|------------------|----------------------|--------------|---------------|-----|
| DEC PDP-8           | 12                | $pdp8Cycles              | $pdp8Insts                   | $pdp8Reads            | $pdp8Writes             | $pdp8Cpi |
| IBM System/360      | 32                | $ibmCycles              | $ibmInsts                   | $ibmReads            | $ibmWrites             | $ibmCpi |
| Cray-1              | 64 (Vector)       | $crayCycles              | $crayInsts                   | $crayReads            | $crayWrites             | $crayCpi |
| Motorola 68000      | 32                | $m68kCycles              | $m68kInsts                   | $m68kReads            | $m68kWrites             | $m68kCpi |
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
