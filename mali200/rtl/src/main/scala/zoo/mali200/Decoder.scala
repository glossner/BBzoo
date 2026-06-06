package zoo.mali200

import chisel3._
import chisel3.util._

class Mali200CtrlSignals extends Bundle {
  val opcode   = UInt(8.W)
  val dest_v   = UInt(2.W)
  val src1_v   = UInt(2.W)
  val src2_v   = UInt(2.W)
  val is_vld   = Bool()
  val is_vst   = Bool()
  val is_vadd  = Bool()
  val is_hlt   = Bool()
  val legal    = Bool()
}

class Mali200Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new Mali200CtrlSignals)
  })

  val op = io.inst(31, 24)
  io.ctrl.opcode := op
  io.ctrl.dest_v := io.inst(23, 22)
  io.ctrl.src1_v := io.inst(19, 18)
  io.ctrl.src2_v := io.inst(15, 14)

  io.ctrl.is_vld  := op === 0x10.U
  io.ctrl.is_vadd := op === 0x20.U
  io.ctrl.is_vst  := op === 0x30.U
  io.ctrl.is_hlt  := op === 0x00.U

  io.ctrl.legal := (io.ctrl.opcode === 0.U || io.ctrl.opcode === 10.U || io.ctrl.opcode === 16.U || io.ctrl.opcode === 20.U || io.ctrl.opcode === 30.U || io.ctrl.opcode === 32.U || io.ctrl.opcode === 48.U)
}
