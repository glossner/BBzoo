package zoo.crusoe

import chisel3._
import chisel3.util._

class Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))

    val op      = Output(UInt(4.W))
    val rd      = Output(UInt(4.W))
    val rs      = Output(UInt(4.W))
    val target  = Output(UInt(16.W))
  })

  io.op     := io.inst(31, 28)
  io.rd     := io.inst(27, 24)
  io.rs     := io.inst(23, 20)
  io.target := io.inst(15, 0)
}
