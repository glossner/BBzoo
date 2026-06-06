package zoo.babbage

import chisel3._
import chisel3.util._
import zoo.common.components.AluOp

class BabbageCtrlSignals extends Bundle {
  val mem_read  = Bool()
  val mem_write = Bool()
  val alu_op    = UInt(4.W)
  val is_hlt    = Bool()
  val legal    = Bool()
}

class BabbageDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(64.W))
    val ctrl = Output(new BabbageCtrlSignals)
    val addr = Output(UInt(16.W))
  })

  // Encode as: opcode (upper 32 bits, but fits in 8 bits opcode), address (lower 16 bits)
  val opcode = io.inst(63, 32)
  io.addr := io.inst(15, 0)

  // Default assignments
  io.ctrl.mem_read  := false.B
  io.ctrl.mem_write := false.B
  io.ctrl.alu_op    := AluOp.PASS_B
  io.ctrl.is_hlt    := false.B

  io.ctrl.legal := false.B
  switch(opcode) {
    is(1.U) {
      io.ctrl.legal := true.B
       // L addr
      io.ctrl.mem_read := true.B
    }
    is(2.U) {
      io.ctrl.legal := true.B
       // S addr
      io.ctrl.mem_write := true.B
    }
    is(3.U) {
      io.ctrl.legal := true.B
       // ADD addr
      io.ctrl.mem_read := true.B
      io.ctrl.alu_op   := AluOp.ADD
    }
    is(4.U) {
      io.ctrl.legal := true.B
       // SUB addr
      io.ctrl.mem_read := true.B
      io.ctrl.alu_op   := AluOp.SUB
    }
    is(5.U) {
      io.ctrl.legal := true.B
       // HLT
      io.ctrl.is_hlt   := true.B
    }
  }
}
