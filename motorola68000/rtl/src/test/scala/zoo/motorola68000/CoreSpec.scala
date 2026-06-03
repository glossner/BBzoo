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
    val hexPath = "motorola68000/sw/test_add.hex"
    val programWords = try {
      val source = Source.fromFile(hexPath)
      val lines = source.getLines().map(_.trim).filter(_.nonEmpty).toList
      source.close()
      lines.map(line => Integer.parseInt(line, 16))
    } catch {
      case _: Exception =>
        List(
          0x207C, 0x0000, 0x1000, // MOVEA.L #0x1000, A0
          0x203C, 0x0000, 0x000F, // MOVE.L #15, D0
          0x223C, 0x0000, 0x001B, // MOVE.L #27, D1
          0xD081,                 // ADD.L D1, D0
          0x2080,                 // MOVE.L D0, (A0)
          0x60FE                  // BRA -2
        )
    }

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

        if (pc == 22) {
          val valHigh = memory.getOrElse(0x1000L, 0)
          val valLow = memory.getOrElse(0x1002L, 0)
          val storedVal = (valHigh << 16) | valLow
          if (storedVal == 42) {
            loopDetected = true
          }
        }

        dut.clock.step(1)
        cycles += 1
      }

      dut.io.debug_regs(0).peek().litValue shouldBe 42
      dut.io.debug_regs(8).peek().litValue shouldBe 0x1000

      val valHigh = memory.getOrElse(0x1000L, 0)
      val valLow = memory.getOrElse(0x1002L, 0)
      val storedVal = (valHigh << 16) | valLow
      storedVal shouldBe 42
    }
  }
}
