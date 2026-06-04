package zoo.itanium

import chisel3._
import chisel3.util._

class Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))

    val op      = Output(UInt(4.W))
    val rd      = Output(UInt(8.W))
    val rs1     = Output(UInt(8.W))
    val rs2     = Output(UInt(8.W))
    val target  = Output(UInt(16.W))
  })

  io.op     := io.inst(27, 24)
  io.rd     := io.inst(23, 16)
  io.rs1    := io.inst(15, 8)
  io.rs2    := io.inst(7, 0)
  io.target := io.inst(15, 0)
}
