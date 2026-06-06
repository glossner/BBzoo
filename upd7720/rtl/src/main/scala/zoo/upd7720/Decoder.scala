package zoo.upd7720

import chisel3._
import chisel3.util._

class Upd7720CtrlSignals extends Bundle {
  val opcode   = UInt(8.W)
  val is_lda   = Bool()
  val is_ldb   = Bool()
  val is_add   = Bool()
  val is_sta   = Bool()
  val is_hlt   = Bool()
  val legal    = Bool()
}

class Upd7720Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(16.W))
    val ctrl = Output(new Upd7720CtrlSignals)
  })

  val op = io.inst(15, 8)

  io.ctrl.opcode := op
  io.ctrl.is_lda := op === 0x10.U
  io.ctrl.is_ldb := op === 0x11.U
  io.ctrl.is_add := op === 0x12.U
  io.ctrl.is_sta := op === 0x13.U
  io.ctrl.is_hlt := op === 0x00.U

  io.ctrl.legal := (io.ctrl.opcode === 0.U || io.ctrl.opcode === 10.U || io.ctrl.opcode === 11.U || io.ctrl.opcode === 12.U || io.ctrl.opcode === 13.U || io.ctrl.opcode === 16.U || io.ctrl.opcode === 17.U || io.ctrl.opcode === 18.U || io.ctrl.opcode === 19.U)
}
