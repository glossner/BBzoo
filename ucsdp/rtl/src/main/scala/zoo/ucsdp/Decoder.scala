package zoo.ucsdp

import chisel3._
import chisel3.util._

class UcsdpCtrlSignals extends Bundle {
  val opcode   = UInt(8.W)
  val is_push  = Bool()
  val is_pop   = Bool()
  val is_add   = Bool()
  val is_hlt   = Bool()
}

class UcsdpDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(16.W))
    val ctrl = Output(new UcsdpCtrlSignals)
  })

  val op = io.inst(15, 8)

  io.ctrl.opcode  := op
  io.ctrl.is_push := op === 0x80.U
  io.ctrl.is_pop  := op === 0x90.U
  io.ctrl.is_add  := op === 0xA0.U
  io.ctrl.is_hlt  := op === 0x00.U
}
