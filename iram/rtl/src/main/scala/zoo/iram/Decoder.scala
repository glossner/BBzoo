package zoo.iram

import chisel3._
import chisel3.util._

class IramCtrlSignals extends Bundle {
  val opcode   = UInt(6.W)
  val rd       = UInt(5.W) // For VLD/VST, this is vd/vs; for LW/SW, it is scalar rd/rs
  val rs1      = UInt(5.W) // For VLD/VST, this is scalar rs/rd
  val rs2      = UInt(5.W) // For VADD.W, this is vb
  val is_vld   = Bool()
  val is_vst   = Bool()
  val is_vadd  = Bool()
  val is_lw    = Bool()
  val is_sw    = Bool()
  val is_hlt   = Bool()
  val legal    = Bool()
}

class IramDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new IramCtrlSignals)
  })

  val op = io.inst(31, 26)

  io.ctrl.opcode  := op
  io.ctrl.rd      := io.inst(25, 21)
  io.ctrl.rs1     := io.inst(20, 16)
  io.ctrl.rs2     := io.inst(15, 11)
  io.ctrl.is_vld  := op === 0x01.U
  io.ctrl.is_vst  := op === 0x02.U
  io.ctrl.is_vadd := op === 0x03.U
  io.ctrl.is_lw   := op === 0x04.U
  io.ctrl.is_sw   := op === 0x05.U
  io.ctrl.is_hlt  := op === 0x3F.U

  io.ctrl.legal := (io.ctrl.opcode === 0.U || io.ctrl.opcode === 1.U || io.ctrl.opcode === 2.U || io.ctrl.opcode === 3.U || io.ctrl.opcode === 4.U || io.ctrl.opcode === 5.U || io.ctrl.opcode === 63.U)
}
