package zoo.hp3000

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.io.Source

class CoreSpec extends AnyFlatSpec with Matchers {
  behavior of "Hp3000Core"

  it should "execute PUSH, POP, ADD, HALT instructions correctly to perform vector addition" in {
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

    val hexFile = findWorkspaceFile("hp3000/sw/test_vector.hex")
    val lines = Source.fromFile(hexFile).getLines()
      .map(_.trim)
      .filter(line => line.nonEmpty && !line.startsWith("#"))
      .map(line => java.lang.Integer.parseInt(line, 16))
      .toArray

    val memSize = 256
    val mem = Array.fill(memSize)(0)
    for (i <- lines.indices) {
      mem(i) = lines(i)
    }

    simulate(new Hp3000Core) { c =>
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
      mem(48) shouldBe 11
      mem(49) shouldBe 22
      mem(50) shouldBe 33
      mem(51) shouldBe 44
    }
  }

  it should "throw an assertion error on unimplemented/illegal instructions" in {
    assertThrows[Throwable] {
      simulate(new Hp3000Core) { c =>
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
            c.io.mem.rdata.poke(BigInt("100", 16).U)
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
