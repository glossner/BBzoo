package zoo.powerpc

import chisel3._
import chisel3.util._

class PowerpcCtrlSignals extends Bundle {
  val opcode = UInt(6.W)
  val rd     = UInt(5.W)
  val rs1    = UInt(5.W)
  val rs2    = UInt(5.W)
  val is_hlt = Bool()
  val legal    = Bool()
}

class PowerpcDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new PowerpcCtrlSignals)
  })

  val opcode = io.inst(31, 26)
  io.ctrl.opcode := opcode
  io.ctrl.rd     := io.inst(25, 21)
  io.ctrl.rs1    := io.inst(20, 16)
  io.ctrl.rs2    := io.inst(15, 11)
  io.ctrl.is_hlt := opcode === 0x3F.U

  io.ctrl.legal := (io.ctrl.opcode === 0.U || io.ctrl.opcode === 1.U || io.ctrl.opcode === 2.U || io.ctrl.opcode === 3.U || io.ctrl.opcode === 63.U)
}
