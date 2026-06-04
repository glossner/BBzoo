package zoo.decpdp8

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.io.Source

class CoreSpec extends AnyFlatSpec with Matchers {
  behavior of "Pdp8Core"

  it should "execute test_add.hex program correctly" in {
    simulate(new Pdp8Core) { c =>
      // Load program from hex file
      val hexFilePath = "/home/jglossner/GitRepos/BrooksZoo/decpdp8/sw/test_add.hex"
      val lines = Source.fromFile(hexFilePath).getLines()
        .map(_.trim)
        .filterNot(line => line.isEmpty || line.startsWith("#"))
        .map(line => Integer.parseInt(line, 16))
        .toArray

      val memSize = 4096
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
        // Evaluate memory request
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
      mem(22) shouldBe 11 // Result: 10 + 1 = 11
      mem(23) shouldBe 22 // Result: 20 + 2 = 22
      mem(24) shouldBe 33 // Result: 30 + 3 = 33
      mem(25) shouldBe 44 // Result: 40 + 4 = 44
      c.io.acc_debug.peek().litValue.toInt shouldBe 0 // DCA clears accumulator
    }
  }
}
