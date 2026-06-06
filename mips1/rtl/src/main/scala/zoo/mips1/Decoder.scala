package zoo.mips1

import chisel3._
import chisel3.util._

class Mips1CtrlSignals extends Bundle {
  val opcode   = UInt(6.W)
  val rs       = UInt(5.W)
  val rt       = UInt(5.W)
  val rd       = UInt(5.W)
  val funct    = UInt(6.W)
  val is_lw    = Bool()
  val is_sw    = Bool()
  val is_addu  = Bool()
  val is_hlt   = Bool()
  val legal    = Bool()
}

class Mips1Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new Mips1CtrlSignals)
  })

  val op = io.inst(31, 26)
  val rs = io.inst(25, 21)
  val rt = io.inst(20, 16)
  val rd = io.inst(15, 11)
  val funct = io.inst(5, 0)

  io.ctrl.opcode := op
  io.ctrl.rs     := rs
  io.ctrl.rt     := rt
  io.ctrl.rd     := rd
  io.ctrl.funct  := funct

  io.ctrl.is_lw   := op === 0x23.U
  io.ctrl.is_sw   := op === 0x2B.U
  io.ctrl.is_addu := (op === 0.U) && (funct === 0x21.U)
  io.ctrl.is_hlt  := op === 0x3F.U

  io.ctrl.legal := (io.ctrl.opcode === 0.U || io.ctrl.opcode === 21.U || io.ctrl.opcode === 23.U || io.ctrl.opcode === 33.U || io.ctrl.opcode === 35.U || io.ctrl.opcode === 43.U || io.ctrl.opcode === 63.U)
}
