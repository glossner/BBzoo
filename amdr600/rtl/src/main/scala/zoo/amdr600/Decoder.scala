package zoo.amdr600

import chisel3._
import chisel3.util._

class Amdr600CtrlSignals extends Bundle {
  val opA      = UInt(4.W)
  val destA    = UInt(2.W)
  val src1A    = UInt(2.W)
  val src2A    = UInt(2.W)
  val opB      = UInt(4.W)
  val regB     = UInt(2.W)
  val addrB    = UInt(8.W)
  
  val is_add   = Bool()
  val is_hlt   = Bool()
  val is_ld    = Bool()
  val is_st    = Bool()
}

class Amdr600Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new Amdr600CtrlSignals)
  })

  io.ctrl.opA   := io.inst(31, 28)
  io.ctrl.destA := io.inst(25, 24)
  io.ctrl.src1A := io.inst(21, 20)
  io.ctrl.src2A := io.inst(17, 16)

  io.ctrl.opB   := io.inst(15, 12)
  io.ctrl.regB  := io.inst(9, 8)
  io.ctrl.addrB := io.inst(7, 0)

  io.ctrl.is_add := io.ctrl.opA === 1.U
  io.ctrl.is_hlt := io.ctrl.opA === 2.U
  io.ctrl.is_ld  := io.ctrl.opB === 1.U
  io.ctrl.is_st  := io.ctrl.opB === 2.U
}
