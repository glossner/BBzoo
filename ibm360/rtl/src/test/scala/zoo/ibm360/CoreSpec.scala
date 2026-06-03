package zoo.ibm360

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.io.Source

class CoreSpec extends AnyFlatSpec with Matchers {
  behavior of "Ibm360Core"

  it should "execute variable-length instructions and perform addition correctly" in {
    simulate(new Ibm360Core) { c =>
      // Load program from hex file as bytes
      val hexFilePath = "/home/jglossner/GitRepos/BrooksZoo/ibm360/sw/test_add.hex"
      val lines = Source.fromFile(hexFilePath).getLines()
        .map(_.trim)
        .filterNot(line => line.isEmpty || line.startsWith("#"))
        .map(line => Integer.parseInt(line, 16).toByte)
        .toArray

      val memSize = 1024
      val mem = Array.fill(memSize)(0.toByte)
      for (i <- lines.indices) {
        mem(i) = lines(i)
      }

      // Helper function to read a 32-bit big-endian word from byte memory
      def readWord(addr: Int): Int = {
        if (addr + 3 >= memSize) return 0
        ((mem(addr) & 0xFF) << 24) |
        ((mem(addr + 1) & 0xFF) << 16) |
        ((mem(addr + 2) & 0xFF) << 8) |
        (mem(addr + 3) & 0xFF)
      }

      // Helper function to write a 32-bit big-endian word to byte memory
      def writeWord(addr: Int, data: Int): Unit = {
        if (addr + 3 >= memSize) return
        mem(addr)     = ((data >> 24) & 0xFF).toByte
        mem(addr + 1) = ((data >> 16) & 0xFF).toByte
        mem(addr + 2) = ((data >> 8) & 0xFF).toByte
        mem(addr + 3) = (data & 0xFF).toByte
      }

      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)

      // Reset sequence
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var cycles = 0
      val maxCycles = 300
      var loopDetected = false

      while (cycles < maxCycles && !loopDetected) {
        val req   = c.io.mem.req.peek().litToBoolean
        val addr  = c.io.mem.addr.peek().litValue.toInt
        val write = c.io.mem.write.peek().litToBoolean
        val wdata = c.io.mem.wdata.peek().litValue.toInt

        if (req) {
          c.io.mem.ready.poke(true.B)
          if (write) {
            writeWord(addr, wdata)
          }
          val rdata = readWord(addr)
          c.io.mem.rdata.poke((rdata.toLong & 0xFFFFFFFFL).U)
        } else {
          c.io.mem.ready.poke(false.B)
        }

        c.clock.step(1)
        cycles += 1

        // Detect if PC branched to address 14 (our infinite branch loop)
        val currentPC = c.io.pc_debug.peek().litValue.toInt
        if (currentPC == 14) {
          loopDetected = true
        }
      }

      loopDetected shouldBe true

      // Verify that GPR1 now contains the result (42 + 24 = 66)
      // And memory[40] contains the result 66
      val resultInMem = readWord(40)
      resultInMem shouldBe 66
    }
  }
}
