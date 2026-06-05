package zoo.execube

import chisel3._
import chisel3.util._

class ExecubeCtrlSignals extends Bundle {
  val opcode   = UInt(6.W)
  val rd       = UInt(2.W) // PE index (0-3)
  val rs1      = UInt(2.W) // PE index (0-3)
  val rs2      = UInt(2.W) // PE index (0-3)
  val is_load  = Bool()
  val is_add   = Bool()
  val is_store = Bool()
  val is_hlt   = Bool()
}

class ExecubeDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new ExecubeCtrlSignals)
  })

  val op = io.inst(31, 26)

  io.ctrl.opcode   := op
  io.ctrl.rd       := io.inst(22, 21) // Extract 2 bits for register (PE) index
  io.ctrl.rs1      := io.inst(17, 16)
  io.ctrl.rs2      := io.inst(12, 11)
  io.ctrl.is_load  := op === 0x01.U
  io.ctrl.is_add   := op === 0x02.U
  io.ctrl.is_store := op === 0x03.U
  io.ctrl.is_hlt   := op === 0x3F.U
}
