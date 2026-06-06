package zoo.decvax

import chisel3._
import chisel3.util._

class DecvaxCtrlSignals extends Bundle {
  val opcode   = UInt(8.W)
  val src_mode = UInt(4.W)
  val src_reg  = UInt(4.W)
  val dst_mode = UInt(4.W)
  val dst_reg  = UInt(4.W)
  val is_hlt   = Bool()
  val legal    = Bool()
}

class DecvaxDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new DecvaxCtrlSignals)
  })

  // Packed VAX Word format:
  // Opcode (31-24) | Src Mode (23-20) | Src Reg (19-16) | Dst Mode (15-12) | Dst Reg (11-8) | Unused (7-0)
  val opcode = io.inst(31, 24)
  io.ctrl.opcode   := opcode
  io.ctrl.src_mode := io.inst(23, 20)
  io.ctrl.src_reg  := io.inst(19, 16)
  io.ctrl.dst_mode := io.inst(15, 12)
  io.ctrl.dst_reg  := io.inst(11, 8)
  
  // HALT is opcode 0x00
  io.ctrl.is_hlt   := opcode === 0.U

  io.ctrl.legal := (io.ctrl.opcode === 0.U || io.ctrl.opcode === 192.U || io.ctrl.opcode === 194.U || io.ctrl.opcode === 208.U)
}
