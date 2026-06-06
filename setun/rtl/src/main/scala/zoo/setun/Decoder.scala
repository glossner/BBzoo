package zoo.setun

import chisel3._
import chisel3.util._

class SetunCtrlSignals extends Bundle {
  val opcode   = UInt(8.W)
  val is_load  = Bool()
  val is_store = Bool()
  val is_add   = Bool()
  val is_hlt   = Bool()
  val legal    = Bool()
}

class SetunDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new SetunCtrlSignals)
  })

  val op = io.inst(31, 24)

  io.ctrl.opcode   := op
  io.ctrl.is_load  := op === 0x10.U
  io.ctrl.is_store := op === 0x11.U
  io.ctrl.is_add   := op === 0x12.U
  io.ctrl.is_hlt   := op === 0xFF.U

  io.ctrl.legal := (io.ctrl.opcode === 0.U || io.ctrl.opcode === 16.U || io.ctrl.opcode === 17.U || io.ctrl.opcode === 18.U || io.ctrl.opcode === 255.U)
}
