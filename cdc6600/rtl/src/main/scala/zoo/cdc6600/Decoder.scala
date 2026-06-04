package zoo.cdc6600

import chisel3._
import chisel3.util._

class Cdc6600CtrlSignals extends Bundle {
  val opcode = UInt(6.W)
  val i      = UInt(3.W)
  val j      = UInt(3.W)
  val k      = UInt(3.W)
  val K      = UInt(18.W)
  val is_hlt = Bool()
}

class Cdc6600Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(60.W))
    val ctrl = Output(new Cdc6600CtrlSignals)
  })

  io.ctrl.opcode := io.inst(59, 54)
  io.ctrl.i      := io.inst(53, 51)
  io.ctrl.j      := io.inst(50, 48)
  io.ctrl.k      := io.inst(47, 45)
  io.ctrl.K      := io.inst(44, 27)
  io.ctrl.is_hlt := io.inst === 0.U
}
