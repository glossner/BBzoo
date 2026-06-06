package zoo.tms320c6k

import chisel3._
import chisel3.util._

class Decoder extends Module {
  val io = IO(new Bundle {
    val legal = Output(Bool())

    val inst = Input(UInt(32.W))

    val op      = Output(UInt(4.W))
    val rd      = Output(UInt(4.W))
    val rs1     = Output(UInt(4.W))
    val rs2     = Output(UInt(4.W))
    val target  = Output(UInt(16.W))
    val p_bit   = Output(Bool())
  })

  io.op     := io.inst(31, 28)
  io.rd     := io.inst(27, 24)
  io.rs1    := io.inst(23, 20)
  io.rs2    := io.inst(19, 16)
  io.target := io.inst(16, 1)
  io.p_bit  := io.inst(0)

  io.legal := (io.op === 0.U || io.op === 1.U || io.op === 2.U || io.op === 3.U || io.op === 4.U || io.op === 5.U || io.op === 6.U || io.op === 7.U)
}
