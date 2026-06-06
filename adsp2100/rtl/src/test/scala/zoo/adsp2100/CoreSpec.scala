package zoo.adsp2100

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.io.Source

class CoreSpec extends AnyFlatSpec with Matchers {
  behavior of "Adsp2100Core"

  def testProgram(program: Array[Int], initMem: Map[Int, Int] = Map.empty, maxCycles: Int = 100)(check: (Adsp2100Core, Array[Int]) => Unit): Unit = {
    val memSize = 256
    val mem = Array.fill(memSize)(0)
    for (i <- program.indices) {
      mem(i) = program(i)
    }
    for ((addr, value) <- initMem) {
      mem(addr) = value
    }

    simulate(new Adsp2100Core) { c =>
      c.io.mem.ready.poke(false.B)
      c.io.mem.rdata.poke(0.U)

      // Reset
      c.reset.poke(true.B)
      c.clock.step(5)
      c.reset.poke(false.B)

      var cycles = 0
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
      check(c, mem)
    }
  }

  it should "execute LD AX0 instruction correctly" in {
    // Program:
    // 0: LD AX0, 0x10 -> Opcode 0x2000, Target Address 0x0010
    // 2: HALT -> Opcode 0x0000
    val program = Array(
      0x2000, 0x0010,
      0x0000
    )
    val initMem = Map(0x0010 -> 0x1234)

    testProgram(program, initMem) { (c, mem) =>
      c.io.ax0_debug.peek().litValue shouldBe 0x1234
      c.io.hlt.peek().litToBoolean shouldBe true
    }
  }

  it should "execute LD AY0 instruction correctly" in {
    // Program:
    // 0: LD AY0, 0x15 -> Opcode 0x2100, Target Address 0x0015
    // 2: HALT -> Opcode 0x0000
    val program = Array(
      0x2100, 0x0015,
      0x0000
    )
    val initMem = Map(0x0015 -> 0x5678)

    testProgram(program, initMem) { (c, mem) =>
      c.io.ay0_debug.peek().litValue shouldBe 0x5678
      c.io.hlt.peek().litToBoolean shouldBe true
    }
  }

  it should "execute ADD AR instruction correctly" in {
    // Program:
    // 0: LD AX0, 0x10 -> Opcode 0x2000, Target Address 0x0010
    // 2: LD AY0, 0x11 -> Opcode 0x2100, Target Address 0x0011
    // 4: ADD AR -> Opcode 0x2200
    // 5: HALT -> Opcode 0x0000
    val program = Array(
      0x2000, 0x0010,
      0x2100, 0x0011,
      0x2200,
      0x0000
    )
    val initMem = Map(
      0x0010 -> 100,
      0x0011 -> 250
    )

    testProgram(program, initMem) { (c, mem) =>
      c.io.ax0_debug.peek().litValue shouldBe 100
      c.io.ay0_debug.peek().litValue shouldBe 250
      c.io.ar_debug.peek().litValue shouldBe 350
      c.io.hlt.peek().litToBoolean shouldBe true
    }
  }

  it should "execute ST AR instruction correctly" in {
    // Program:
    // 0: LD AX0, 0x10
    // 2: LD AY0, 0x11
    // 4: ADD AR
    // 5: ST AR, 0x12 -> Opcode 0x2300, Target Address 0x0012
    // 7: HALT
    val program = Array(
      0x2000, 0x0010,
      0x2100, 0x0011,
      0x2200,
      0x2300, 0x0012,
      0x0000
    )
    val initMem = Map(
      0x0010 -> 10,
      0x0011 -> 20
    )

    testProgram(program, initMem) { (c, mem) =>
      c.io.ar_debug.peek().litValue shouldBe 30
      mem(0x0012) shouldBe 30
      c.io.hlt.peek().litToBoolean shouldBe true
    }
  }

  it should "throw an assertion error on unimplemented/illegal instructions" in {
    // Program:
    // 0: 0xFF00 -> Unknown/unimplemented opcode (should trigger assertion)
    // 1: HALT
    val program = Array(
      0xFF00,
      0x0000
    )

    assertThrows[Throwable] {
      testProgram(program) { (c, mem) =>
        // Should not be reached because simulation fails during sDECODE
      }
    }
  }

  it should "execute LD, ST, ADD, HALT instructions correctly to perform vector addition" in {
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

    val hexFile = findWorkspaceFile("adsp2100/sw/test_vector.hex")
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

    simulate(new Adsp2100Core) { c =>
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
}
