package zoo.powervr1

import chisel3._
import chisel3.util._

class Powervr1CtrlSignals extends Bundle {
  val opcode    = UInt(8.W)
  val dest_reg  = UInt(2.W)
  val src1_reg  = UInt(2.W)
  val src2_reg  = UInt(2.W)
  val depth_reg = UInt(2.W)
  val is_ld     = Bool()
  val is_st     = Bool()
  val is_hsr    = Bool()
  val is_hlt    = Bool()
}

class Powervr1Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new Powervr1CtrlSignals)
  })

  val op = io.inst(31, 24)
  io.ctrl.opcode    := op
  io.ctrl.dest_reg  := io.inst(23, 22)
  io.ctrl.src1_reg  := io.inst(19, 18)
  io.ctrl.src2_reg  := io.inst(15, 14)
  io.ctrl.depth_reg := io.inst(11, 10)

  io.ctrl.is_ld  := op === 0x10.U
  io.ctrl.is_hsr := op === 0x20.U
  io.ctrl.is_st  := op === 0x30.U
  io.ctrl.is_hlt := op === 0x00.U
}
