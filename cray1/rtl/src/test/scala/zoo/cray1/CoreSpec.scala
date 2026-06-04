package zoo.cray1

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.io.Source

class CoreSpec extends AnyFlatSpec with Matchers {
  behavior of "Cray1Core"

  it should "execute vector loads, vector additions, and vector stores correctly" in {
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

    simulate(new Cray1Core) { c =>
      // Load program from hex file as 64-bit values
      val hexFile = findWorkspaceFile("cray1/sw/test_vector.hex")
      val lines = Source.fromFile(hexFile).getLines()
        .map(_.trim)
        .filterNot(line => line.isEmpty || line.startsWith("#"))
        .map(line => java.lang.Long.parseUnsignedLong(line, 16))
        .toArray

      val memSize = 256
      val mem = Array.fill(memSize)(0L)
      for (i <- lines.indices) {
        mem(i) = lines(i)
      }

      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)

      // Reset sequence
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

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
          val rdata = mem(addr)
          // Handle unsigned Long wrapping to BigInt for Chisel poke
          val bigIntData = BigInt(java.lang.Long.toUnsignedString(rdata))
          c.io.mem.rdata.poke(bigIntData.U)
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

      // Verify stored vector elements in memory at address 24 to 27
      mem(24) shouldBe 11L
      mem(25) shouldBe 22L
      mem(26) shouldBe 33L
      mem(27) shouldBe 44L

      // Verify debug values for registers and vector elements
      c.io.vl_debug.peek().litValue.toInt shouldBe 4
      c.io.v0_0_debug.peek().litValue.toLong shouldBe 10L
      c.io.v0_1_debug.peek().litValue.toLong shouldBe 20L
    }
  }
}
