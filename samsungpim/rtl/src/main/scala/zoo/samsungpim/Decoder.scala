package zoo.samsungpim

import chisel3._
import chisel3.util._

class SamsungpimCtrlSignals extends Bundle {
  val opcode   = UInt(6.W)
  val rd       = UInt(2.W) // V0-V3
  val rs1      = UInt(2.W) // V0-V3
  val rs2      = UInt(2.W) // V0-V3
  val is_ld    = Bool()
  val is_st    = Bool()
  val is_add   = Bool()
  val is_hlt   = Bool()
  val legal    = Bool()
}

class SamsungpimDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new SamsungpimCtrlSignals)
  })

  val op = io.inst(31, 26)

  io.ctrl.opcode  := op
  io.ctrl.rd      := io.inst(22, 21) // Extract 2 bits for vector register index
  io.ctrl.rs1     := io.inst(17, 16)
  io.ctrl.rs2     := io.inst(12, 11)
  io.ctrl.is_ld   := op === 0x01.U
  io.ctrl.is_st   := op === 0x02.U
  io.ctrl.is_add  := op === 0x03.U
  io.ctrl.is_hlt  := op === 0x3F.U

  io.ctrl.legal := (io.ctrl.opcode === 0.U || io.ctrl.opcode === 1.U || io.ctrl.opcode === 2.U || io.ctrl.opcode === 3.U || io.ctrl.opcode === 63.U)
}
