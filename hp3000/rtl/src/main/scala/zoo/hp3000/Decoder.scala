package zoo.hp3000

import chisel3._
import chisel3.util._

class Hp3000CtrlSignals extends Bundle {
  val opcode   = UInt(8.W)
  val is_push  = Bool()
  val is_pop   = Bool()
  val is_add   = Bool()
  val is_hlt   = Bool()
  val legal    = Bool()
}

class Hp3000Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(16.W))
    val ctrl = Output(new Hp3000CtrlSignals)
  })

  val op = io.inst(15, 8)

  io.ctrl.opcode  := op
  io.ctrl.is_push := op === 0x20.U
  io.ctrl.is_pop  := op === 0x40.U
  io.ctrl.is_add  := op === 0x60.U
  io.ctrl.is_hlt  := op === 0x00.U

  io.ctrl.legal := (io.ctrl.opcode === 0.U || io.ctrl.opcode === 20.U || io.ctrl.opcode === 32.U || io.ctrl.opcode === 40.U || io.ctrl.opcode === 60.U || io.ctrl.opcode === 64.U || io.ctrl.opcode === 96.U)
}
