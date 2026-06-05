package zoo.tta

import chisel3._
import chisel3.util._

class TtaCtrlSignals extends Bundle {
  val opcode       = UInt(6.W)
  val src          = UInt(6.W)
  val dest         = UInt(6.W)
  val is_move      = Bool()
  val is_move_imm  = Bool()
  val is_hlt       = Bool()
}

class TtaDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new TtaCtrlSignals)
  })

  val op = io.inst(31, 26)

  io.ctrl.opcode      := op
  io.ctrl.src         := io.inst(25, 20)
  io.ctrl.dest        := io.inst(19, 14)
  io.ctrl.is_move     := op === 0x00.U
  io.ctrl.is_move_imm := op === 0x01.U
  io.ctrl.is_hlt      := op === 0x3F.U
}
