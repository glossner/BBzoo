package zoo.ibmmwave

import chisel3._
import chisel3.util._

class IbmmwaveCtrlSignals extends Bundle {
  val opcode  = UInt(8.W)
  val is_ldr1 = Bool()
  val is_ldr2 = Bool()
  val is_add  = Bool()
  val is_str1 = Bool()
  val is_hlt  = Bool()
  val legal    = Bool()
}

class IbmmwaveDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(16.W))
    val ctrl = Output(new IbmmwaveCtrlSignals)
  })

  val op = io.inst(15, 8)

  io.ctrl.opcode  := op
  io.ctrl.is_ldr1 := op === 0x30.U
  io.ctrl.is_ldr2 := op === 0x31.U
  io.ctrl.is_add  := op === 0x32.U
  io.ctrl.is_str1 := op === 0x33.U
  io.ctrl.is_hlt  := op === 0x00.U

  io.ctrl.legal := (io.ctrl.opcode === 0.U || io.ctrl.opcode === 30.U || io.ctrl.opcode === 31.U || io.ctrl.opcode === 32.U || io.ctrl.opcode === 33.U || io.ctrl.opcode === 48.U || io.ctrl.opcode === 49.U || io.ctrl.opcode === 50.U || io.ctrl.opcode === 51.U)
}
