package zoo.harvardmark1

import chisel3._
import chisel3.util._
import zoo.common.components.AluOp

class HarvardMark1CtrlSignals extends Bundle {
  val is_hlt = Bool()
  val write_reg = Bool()
  val alu_op = UInt(4.W)
  val legal    = Bool()
}

class HarvardMark1Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(64.W))
    val ctrl = Output(new HarvardMark1CtrlSignals)
    val src  = Output(UInt(8.W))
    val dst  = Output(UInt(8.W))
  })

  // Format: opcode (bits 23-16), src (bits 15-8), dst (bits 7-0)
  val opcode = io.inst(23, 16)
  io.src   := io.inst(15, 8)
  io.dst   := io.inst(7, 0)

  // Defaults
  io.ctrl.is_hlt    := false.B
  io.ctrl.write_reg := false.B
  io.ctrl.alu_op    := AluOp.PASS_B

  io.ctrl.legal := false.B
  switch(opcode) {
    is(1.U) {
      io.ctrl.legal := true.B
       // MOV src, dst
      io.ctrl.write_reg := true.B
    }
    is(2.U) {
      io.ctrl.legal := true.B
       // ADD src, dst
      io.ctrl.write_reg := true.B
      io.ctrl.alu_op    := AluOp.ADD
    }
    is(3.U) {
      io.ctrl.legal := true.B
       // SUB src, dst
      io.ctrl.write_reg := true.B
      io.ctrl.alu_op    := AluOp.SUB
    }
    is(4.U) {
      io.ctrl.legal := true.B
       // HLT
      io.ctrl.is_hlt    := true.B
    }
  }
}
