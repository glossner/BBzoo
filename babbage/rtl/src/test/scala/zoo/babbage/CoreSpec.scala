package zoo.babbage

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.io.Source

class CoreSpec extends AnyFlatSpec with Matchers {
  behavior of "BabbageCore"

  it should "execute L, S, ADD, SUB correctly on 64-bit values" in {
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

    val hexFile = findWorkspaceFile("babbage/sw/test_vector.hex")
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

    simulate(new BabbageCore) { c =>
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)

      // Reset
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
      mem(28) shouldBe 11L
      mem(29) shouldBe 22L
      mem(30) shouldBe 33L
      mem(31) shouldBe 44L
    }
  }
}
