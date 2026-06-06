package zoo.ibm360

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.io.Source

class CoreSpec extends AnyFlatSpec with Matchers {
  behavior of "Ibm360Core"

  it should "execute variable-length instructions and perform addition correctly" in {
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

    simulate(new Ibm360Core) { c =>
      // Load program from hex file as bytes
      val hexFile = findWorkspaceFile("ibm360/sw/test_add.hex")
      val lines = Source.fromFile(hexFile).getLines()
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

        // Detect if PC branched to address 48 (our infinite branch loop)
        val currentPC = c.io.pc_debug.peek().litValue.toInt
        if (currentPC == 48) {
          loopDetected = true
        }
      }

      loopDetected shouldBe true

      // Verify that Vector C now contains the results
      readWord(84) shouldBe 11
      readWord(88) shouldBe 22
      readWord(92) shouldBe 33
      readWord(96) shouldBe 44
    }
  }

  it should "throw an assertion error on unimplemented/illegal instructions" in {
    assertThrows[Throwable] {
      simulate(new Ibm360Core) { c =>
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
            c.io.mem.rdata.poke(BigInt("20000000000", 16).U)
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
