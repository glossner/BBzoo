package zoo.intel3002

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.io.Source

class CoreSpec extends AnyFlatSpec with Matchers {
  behavior of "Intel3002Core"

  it should "execute test_vector.hex program correctly" in {
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

    simulate(new Intel3002Core) { c =>
      // Load program from hex file
      val hexFile = findWorkspaceFile("intel3002/sw/test_vector.hex")
      val lines = Source.fromFile(hexFile).getLines()
        .map(_.trim)
        .filterNot(line => line.isEmpty || line.startsWith("#"))
        .map(line => Integer.parseInt(line, 16))
        .toArray

      val memSize = 256
      val mem = Array.fill(memSize)(0)
      for (i <- lines.indices) {
        mem(i) = lines(i)
      }

      // Initialize inputs
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
        val wdata = c.io.mem.wdata.peek().litValue.toInt

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
      mem(37) shouldBe 11 // C0
      mem(38) shouldBe 22 // C1
      mem(39) shouldBe 33 // C2
      mem(40) shouldBe 44 // C3
    }
  }
}
