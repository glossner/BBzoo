package zoo.cdcstar100

import chisel3._
import chisel3.util._

class Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val op   = Output(UInt(4.W))
    val rc   = Output(UInt(3.W))
    val ra   = Output(UInt(3.W))
    val rb   = Output(UInt(3.W))
    val rlen = Output(UInt(3.W))
    val target = Output(UInt(16.W))
  })

  io.op   := io.inst(31, 28)
  io.rc   := io.inst(26, 24)
  io.ra   := io.inst(22, 20)
  io.rb   := io.inst(18, 16)
  io.rlen := io.inst(14, 12)
  io.target := io.inst(15, 0)
}
