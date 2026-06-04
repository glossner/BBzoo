package zoo.harvardmark1

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.io.Source

class CoreSpec extends AnyFlatSpec with Matchers {
  behavior of "HarvardMark1Core"

  it should "execute register transfer and addition correctly" in {
    def findWorkspaceFile(relativePath: String): java.io.File = {
      var dir = new java.io.File(System.getProperty("user.dir"))
      var foundFile = new java.io.File(dir, relativePath)
      while (dir != null && !foundFile.exists()) {
        dir = dir.getParentFile
        if (dir != null) {
          foundFile = new java.io.File(dir, relativePath)
        }
      }
      if (foundFile.exists()) foundFile else new java.io.File(relativePath)
    }

    val hexFile = findWorkspaceFile("harvardmark1/sw/test_vector.hex")
    val lines = Source.fromFile(hexFile).getLines()
      .map(_.trim)
      .filter(line => line.nonEmpty && !line.startsWith("#"))
      .map(line => java.lang.Long.parseUnsignedLong(line, 16))
      .toArray

    val memSize = 256
    val mem = Array.fill(memSize)(0L)
    for (i <- lines.indices) {
      mem(i) = lines(i)
    }

    simulate(new HarvardMark1Core) { c =>
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)
      c.io.test_wen.poke(false.B)
      c.io.test_waddr.poke(0.U)
      c.io.test_wdata.poke(0.U)

      // Reset
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      // Pre-load input vector values using the test-write port
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

      var cycles = 0
      val maxCycles = 500
      var halted = false

      while (cycles < maxCycles && !halted) {
        val req   = c.io.mem.req.peek().litToBoolean
        val addr  = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toLong

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) {
            mem(addr) = wdata
          }
          c.io.mem.rdata.poke(mem(addr).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        cycles += 1

        if (c.io.hlt.peek().litToBoolean) {
          halted = true
        }
      }

      halted shouldBe true
      
      // Verify outputs in registers R28-R31 (Vector C)
      c.io.regs_debug(28).peek().litValue shouldBe 11
      c.io.regs_debug(29).peek().litValue shouldBe 22
      c.io.regs_debug(30).peek().litValue shouldBe 33
      c.io.regs_debug(31).peek().litValue shouldBe 44
    }
  }
}
