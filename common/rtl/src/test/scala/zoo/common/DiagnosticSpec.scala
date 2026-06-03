package zoo.common

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PassThrough extends Module {
  val io = IO(new Bundle {
    val in  = Input(UInt(8.W))
    val out = Output(UInt(8.W))
  })
  io.out := io.in
}

class DiagnosticSpec extends AnyFlatSpec with Matchers {
  behavior of "PassThrough"

  it should "pass input to output in simulation" in {
    simulate(new PassThrough) { dut =>
      dut.io.in.poke(42.U)
      dut.clock.step(1)
      dut.io.out.peek().litValue shouldBe 42
    }
  }
}
