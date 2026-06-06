package zoo.motorola6800

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.io.Source

class CoreSpec extends AnyFlatSpec with Matchers {
  behavior of "Motorola6800Core"

  it should "execute LDAA, STAA, ADDA, WAI instructions correctly on 8-bit values" in {
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

    val hexFile = findWorkspaceFile("motorola6800/sw/test_vector.hex")
    val lines = Source.fromFile(hexFile).getLines()
      .map(_.trim)
      .filter(line => line.nonEmpty && !line.startsWith("#"))
      .map(line => Integer.parseInt(line, 16))
      .toArray

    val memSize = 65536
    val mem = Array.fill(memSize)(0)
    for (i <- lines.indices) {
      mem(i) = lines(i)
    }

    simulate(new Motorola6800Core) { c =>
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
      mem(88) shouldBe 11
      mem(89) shouldBe 22
      mem(90) shouldBe 33
      mem(91) shouldBe 44
    }
  }

  it should "throw an assertion error on unimplemented/illegal instructions" in {
    assertThrows[Throwable] {
      simulate(new Motorola6800Core) { c =>
        c.io.mem.ready.poke(false.B)
        c.io.mem.rdata.poke(0.U)

        c.reset.poke(true.B)
        c.clock.step(5)
        c.reset.poke(false.B)

        var cycles = 0
        while (cycles < 15) {
          val req = c.io.mem.req.peek().litToBoolean
          if (req) {
            c.io.mem.ready.poke(true.B)
            c.io.mem.rdata.poke(BigInt("1", 16).U)
          } else {
            c.io.mem.ready.poke(false.B)
          }
          c.clock.step(1)
          cycles += 1
        }
      }
    }
  }
}
