package zoo.ibm6150

import chisel3._
import chisel3.util._

class Ibm6150CtrlSignals extends Bundle {
  val opcode   = UInt(8.W)
  val reg_x    = UInt(4.W)
  val reg_y    = UInt(4.W)
  val is_hlt   = Bool()
}

class Ibm6150Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new Ibm6150CtrlSignals)
  })

  // Format: Opcode (31-24) | Reg X (23-20) | Reg Y (19-16) | Unused (15-0)
  val opcode = io.inst(31, 24)
  io.ctrl.opcode := opcode
  io.ctrl.reg_x  := io.inst(23, 20)
  io.ctrl.reg_y  := io.inst(19, 16)
  io.ctrl.is_hlt := opcode === 0.U
}
