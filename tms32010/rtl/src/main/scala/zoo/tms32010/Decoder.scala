package zoo.tms32010

import chisel3._
import chisel3.util._

class Tms32010CtrlSignals extends Bundle {
  val opcode   = UInt(8.W)
  val is_lac   = Bool()
  val is_add   = Bool()
  val is_sacl  = Bool()
  val is_hlt   = Bool()
}

class Tms32010Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(16.W))
    val ctrl = Output(new Tms32010CtrlSignals)
  })

  val op = io.inst(15, 8)

  io.ctrl.opcode  := op
  io.ctrl.is_lac  := op === 0x01.U
  io.ctrl.is_add  := op === 0x02.U
  io.ctrl.is_sacl := op === 0x03.U
  io.ctrl.is_hlt  := op === 0x00.U
}
