package zoo.adsp2100

import chisel3._
import chisel3.util._

class Adsp2100CtrlSignals extends Bundle {
  val opcode   = UInt(8.W)
  val is_ldax0 = Bool()
  val is_lday0 = Bool()
  val is_addar = Bool()
  val is_star  = Bool()
  val is_hlt   = Bool()
  val legal    = Bool()
}

class Adsp2100Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(16.W))
    val ctrl = Output(new Adsp2100CtrlSignals)
  })

  val op = io.inst(15, 8)

  io.ctrl.opcode   := op
  io.ctrl.is_ldax0 := op === 0x20.U
  io.ctrl.is_lday0 := op === 0x21.U
  io.ctrl.is_addar := op === 0x22.U
  io.ctrl.is_star  := op === 0x23.U
  io.ctrl.is_hlt   := op === 0x00.U
  io.ctrl.legal    := (op === 0x20.U || op === 0x21.U || op === 0x22.U || op === 0x23.U || op === 0x00.U)
}
