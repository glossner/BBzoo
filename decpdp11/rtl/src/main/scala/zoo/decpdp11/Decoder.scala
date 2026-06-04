package zoo.decpdp11

import chisel3._
import chisel3.util._

class Pdp11CtrlSignals extends Bundle {
  val opcode   = UInt(4.W)
  val src_mode = UInt(3.W)
  val src_reg  = UInt(3.W)
  val dst_mode = UInt(3.W)
  val dst_reg  = UInt(3.W)
  val is_hlt   = Bool()
}

class Pdp11Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(16.W))
    val ctrl = Output(new Pdp11CtrlSignals)
  })

  val opcode = io.inst(15, 12)
  io.ctrl.opcode   := opcode
  io.ctrl.src_mode := io.inst(11, 9)
  io.ctrl.src_reg  := io.inst(8, 6)
  io.ctrl.dst_mode := io.inst(5, 3)
  io.ctrl.dst_reg  := io.inst(2, 0)
  
  // HALT is 0x0000
  io.ctrl.is_hlt   := io.inst === 0.U
}
