package zoo.itanium

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.io.Source

class CoreSpec extends AnyFlatSpec with Matchers {
  behavior of "ItaniumCore"

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

    simulate(new Core) { c =>
      val hexFile = findWorkspaceFile("itanium/sw/test_vector.hex")
      val lines = Source.fromFile(hexFile).getLines()
        .map(_.trim)
        .filterNot(line => line.isEmpty || line.startsWith("#"))
        .map(line => java.lang.Long.parseUnsignedLong(line, 16).toInt)
        .toArray

      val memSize = 256
      val mem = Array.fill(memSize)(0L)
      for (i <- 0 until (lines.length / 2)) {
        val word0 = lines(2 * i).toLong & 0xFFFFFFFFL
        val word1 = lines(2 * i + 1).toLong & 0xFFFFFFFFL
        mem(i) = (word1 << 32) | word0
      }

      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)

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
          val wordAddr = addr >> 1
          if (write) {
            println(s"WRITE: addr = $addr, wdata = $wdata")
            mem(wordAddr) = wdata
          }
          c.io.mem.rdata.poke(mem(wordAddr).U)
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
      mem(50) shouldBe 11L
      mem(51) shouldBe 22L
      mem(52) shouldBe 33L
      mem(53) shouldBe 44L
    }
  }

  it should "throw an assertion error on unimplemented/illegal instructions" in {
    assertThrows[Throwable] {
      simulate(new Core) { c =>
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
            c.io.mem.rdata.poke(BigInt("8000000", 16).U)
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
