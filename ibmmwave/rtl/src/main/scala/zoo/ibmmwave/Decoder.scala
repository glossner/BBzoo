package zoo.mwave

import chisel3._
import chisel3.util._

class MwaveCtrlSignals extends Bundle {
  val opcode  = UInt(8.W)
  val is_ldr1 = Bool()
  val is_ldr2 = Bool()
  val is_add  = Bool()
  val is_str1 = Bool()
  val is_hlt  = Bool()
}

class MwaveDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(16.W))
    val ctrl = Output(new MwaveCtrlSignals)
  })

  val op = io.inst(15, 8)

  io.ctrl.opcode  := op
  io.ctrl.is_ldr1 := op === 0x30.U
  io.ctrl.is_ldr2 := op === 0x31.U
  io.ctrl.is_add  := op === 0x32.U
  io.ctrl.is_str1 := op === 0x33.U
  io.ctrl.is_hlt  := op === 0x00.U
}
