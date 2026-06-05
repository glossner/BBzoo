package zoo.soar

import chisel3._
import chisel3.util._

class SoarCtrlSignals extends Bundle {
  val opcode   = UInt(6.W)
  val rd       = UInt(5.W)
  val rs1      = UInt(5.W)
  val rs2      = UInt(5.W)
  val is_load  = Bool()
  val is_store = Bool()
  val is_add   = Bool()
  val is_hlt   = Bool()
}

class SoarDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new SoarCtrlSignals)
  })

  val op = io.inst(31, 26)

  io.ctrl.opcode   := op
  io.ctrl.rd       := io.inst(25, 21)
  io.ctrl.rs1      := io.inst(20, 16)
  io.ctrl.rs2      := io.inst(15, 11)
  io.ctrl.is_load  := op === 0x02.U
  io.ctrl.is_store := op === 0x03.U
  io.ctrl.is_add   := op === 0x01.U
  io.ctrl.is_hlt   := op === 0x3F.U
}
