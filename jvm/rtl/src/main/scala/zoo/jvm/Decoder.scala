package zoo.jvm

import chisel3._
import chisel3.util._

class JvmCtrlSignals extends Bundle {
  val opcode    = UInt(8.W)
  val is_iload  = Bool()
  val is_istore = Bool()
  val is_iadd   = Bool()
  val is_hlt    = Bool()
}

class JvmDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new JvmCtrlSignals)
  })

  val op = io.inst(31, 24)

  io.ctrl.opcode    := op
  io.ctrl.is_iload  := op === 0x15.U
  io.ctrl.is_istore := op === 0x36.U
  io.ctrl.is_iadd   := op === 0x60.U
  io.ctrl.is_hlt    := op === 0xFF.U
}
