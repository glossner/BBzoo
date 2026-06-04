package zoo.motorola68000

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.io.Source
import java.io.FileNotFoundException
import zoo.common.components._

class CoreSpec extends AnyFlatSpec with Matchers {
  behavior of "M68kCore"

  it should "execute test_add.hex program correctly" in {
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
    val hexFile = findWorkspaceFile("motorola68000/sw/test_add.hex")
    val programWords = Source.fromFile(hexFile).getLines().map(_.trim).filter(line => line.nonEmpty && !line.startsWith("#")).toList.map(line => Integer.parseInt(line, 16))

    simulate(new M68kCore) { dut =>
      dut.reset.poke(true.B)
      dut.clock.step(5)
      dut.reset.poke(false.B)

      val memory = scala.collection.mutable.Map[Long, Int]()

      for ((word, index) <- programWords.zipWithIndex) {
        memory(index * 2L) = word
      }

      var cycles = 0
      val maxCycles = 200
      var loopDetected = false

      while (cycles < maxCycles && !loopDetected) {
        val req = dut.io.mem.req.peek().litValue > 0
        val addr = dut.io.mem.addr.peek().litValue.toLong
        val write = dut.io.mem.write.peek().litValue > 0
        val wdata = dut.io.mem.wdata.peek().litValue.toInt

        if (req) {
          if (write) {
            memory(addr) = wdata
            dut.io.mem.rdata.poke(0.U)
            dut.io.mem.ready.poke(true.B)
          } else {
            val rdata = memory.getOrElse(addr, 0)
            dut.io.mem.rdata.poke(rdata.U(16.W))
            dut.io.mem.ready.poke(true.B)
          }
        } else {
          dut.io.mem.ready.poke(false.B)
        }

        val pc = dut.io.debug_pc.peek().litValue
        val state = dut.io.debug_state.peek().litValue
        val reg0 = dut.io.debug_regs(0).peek().litValue
        val reg1 = dut.io.debug_regs(1).peek().litValue
        val reg8 = dut.io.debug_regs(8).peek().litValue
        println(s"Cycle: $cycles | PC: $pc | State: $state | Req: $req | Addr: $addr | Write: $write | Wdata: $wdata | D0: $reg0 | D1: $reg1 | A0: $reg8")

        if (pc == 104) {
          loopDetected = true
        }

        dut.clock.step(1)
        cycles += 1
      }

      loopDetected shouldBe true

      // Helper to read 32-bit word from 16-bit word memory map
      def read32(addr: Long): Int = {
        val high = memory.getOrElse(addr, 0)
        val low = memory.getOrElse(addr + 2, 0)
        (high << 16) | (low & 0xFFFF)
      }

      read32(168) shouldBe 11
      read32(172) shouldBe 22
      read32(176) shouldBe 33
      read32(180) shouldBe 44
    }
  }
}
