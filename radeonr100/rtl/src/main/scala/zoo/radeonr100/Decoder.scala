package zoo.radeonr100

import chisel3._
import chisel3.util._

class Radeonr100CtrlSignals extends Bundle {
  val opcode   = UInt(8.W)
  val dest_reg = UInt(2.W)
  val srcA_reg = UInt(2.W)
  val srcB_reg = UInt(2.W)
  val srcC_reg = UInt(2.W)
  val is_ld    = Bool()
  val is_st    = Bool()
  val is_tapestry = Bool()
  val is_hlt   = Bool()
  val legal    = Bool()
}

class Radeonr100Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new Radeonr100CtrlSignals)
  })

  val op = io.inst(31, 24)
  io.ctrl.opcode   := op
  io.ctrl.dest_reg := io.inst(23, 22)
  io.ctrl.srcA_reg := io.inst(19, 18)
  io.ctrl.srcB_reg := io.inst(15, 14)
  io.ctrl.srcC_reg := io.inst(11, 10)

  io.ctrl.is_ld       := op === 0x10.U
  io.ctrl.is_tapestry := op === 0x20.U
  io.ctrl.is_st       := op === 0x30.U
  io.ctrl.is_hlt      := op === 0x00.U

  io.ctrl.legal := (io.ctrl.opcode === 0.U || io.ctrl.opcode === 10.U || io.ctrl.opcode === 16.U || io.ctrl.opcode === 20.U || io.ctrl.opcode === 30.U || io.ctrl.opcode === 32.U || io.ctrl.opcode === 48.U)
}
