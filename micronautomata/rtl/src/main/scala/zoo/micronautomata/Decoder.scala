package zoo.micronautomata

import chisel3._
import chisel3.util._

class MicronautomataCtrlSignals extends Bundle {
  val opcode   = UInt(6.W)
  val rd       = UInt(1.W) // R0 or R1
  val rs1      = UInt(1.W) // R0 or R1
  val rs2      = UInt(1.W) // R0 or R1
  val is_in    = Bool()
  val is_add   = Bool()
  val is_out   = Bool()
  val is_hlt   = Bool()
}

class MicronautomataDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new MicronautomataCtrlSignals)
  })

  val op = io.inst(31, 26)

  io.ctrl.opcode := op
  io.ctrl.rd     := io.inst(21) // Extract 1 bit for register index (R0/R1)
  io.ctrl.rs1    := io.inst(16)
  io.ctrl.rs2    := io.inst(11)
  io.ctrl.is_in  := op === 0x01.U
  io.ctrl.is_add := op === 0x02.U
  io.ctrl.is_out := op === 0x03.U
  io.ctrl.is_hlt := op === 0x3F.U
}
