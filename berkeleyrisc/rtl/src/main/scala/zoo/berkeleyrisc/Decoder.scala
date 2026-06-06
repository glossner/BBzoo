package zoo.berkeleyrisc

import chisel3._
import chisel3.util._

class BerkeleyriscCtrlSignals extends Bundle {
  val opcode = UInt(8.W)
  val rd     = UInt(5.W)
  val rs     = UInt(5.W)
  val rm     = UInt(5.W)
  val is_ld  = Bool()
  val is_st  = Bool()
  val is_add = Bool()
  val is_hlt = Bool()
  val legal    = Bool()
}

class BerkeleyriscDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new BerkeleyriscCtrlSignals)
  })

  val op = io.inst(31, 24)
  val rd = io.inst(23, 19)
  val rs = io.inst(18, 14)
  val rm = io.inst(4, 0)

  io.ctrl.opcode := op
  io.ctrl.rd     := rd
  io.ctrl.rs     := rs
  io.ctrl.rm     := rm

  io.ctrl.is_ld  := op === 0x01.U
  io.ctrl.is_st  := op === 0x02.U
  io.ctrl.is_add := op === 0x03.U
  io.ctrl.is_hlt := op === 0x00.U

  io.ctrl.legal := (io.ctrl.opcode === 0.U || io.ctrl.opcode === 1.U || io.ctrl.opcode === 2.U || io.ctrl.opcode === 3.U)
}
