package zoo.arm1

import chisel3._
import chisel3.util._

class Arm1CtrlSignals extends Bundle {
  val cond   = UInt(4.W)
  val opcode = UInt(8.W)
  val rn     = UInt(4.W)
  val rd     = UInt(4.W)
  val rm     = UInt(4.W)
  val is_ldr = Bool()
  val is_str = Bool()
  val is_add = Bool()
  val is_hlt = Bool()
  val legal    = Bool()
}

class Arm1Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new Arm1CtrlSignals)
  })

  val cond   = io.inst(31, 28)
  val op     = io.inst(27, 20)
  val rn     = io.inst(19, 16)
  val rd     = io.inst(15, 12)
  val rm     = io.inst(3, 0)

  io.ctrl.cond   := cond
  io.ctrl.opcode := op
  io.ctrl.rn     := rn
  io.ctrl.rd     := rd
  io.ctrl.rm     := rm

  io.ctrl.is_ldr := op === 0x04.U
  io.ctrl.is_str := op === 0x05.U
  io.ctrl.is_add := op === 0x00.U
  io.ctrl.is_hlt := op === 0x0F.U

  io.ctrl.legal := (io.ctrl.opcode === 0.U || io.ctrl.opcode === 4.U || io.ctrl.opcode === 5.U || io.ctrl.opcode === 14.U || io.ctrl.opcode === 15.U)
}
