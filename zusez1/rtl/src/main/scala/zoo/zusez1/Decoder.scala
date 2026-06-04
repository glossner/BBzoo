package zoo.zusez1

import chisel3._
import chisel3.util._
import zoo.common.components.AluOp

class ZuseZ1CtrlSignals extends Bundle {
  val mem_read  = Bool()
  val mem_write = Bool()
  val alu_op    = UInt(4.W)
  val mov_r1_r2 = Bool()
  val is_hlt    = Bool()
}

class ZuseZ1Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(22.W))
    val ctrl = Output(new ZuseZ1CtrlSignals)
    val addr = Output(UInt(16.W))
  })

  // Format: opcode (bits 21-16), addr (bits 15-0)
  val opcode = io.inst(21, 16)
  io.addr := io.inst(15, 0)

  // Defaults
  io.ctrl.mem_read  := false.B
  io.ctrl.mem_write := false.B
  io.ctrl.alu_op    := AluOp.PASS_A
  io.ctrl.mov_r1_r2 := false.B
  io.ctrl.is_hlt    := false.B

  switch(opcode) {
    is(1.U) { // Pr addr (load to R1)
      io.ctrl.mem_read := true.B
    }
    is(2.U) { // Ps addr (store from R1)
      io.ctrl.mem_write := true.B
    }
    is(3.U) { // ADD
      io.ctrl.alu_op := AluOp.ADD
    }
    is(4.U) { // SUB
      io.ctrl.alu_op := AluOp.SUB
    }
    is(5.U) { // MOV R1, R2
      io.ctrl.mov_r1_r2 := true.B
    }
    is(6.U) { // HLT
      io.ctrl.is_hlt := true.B
    }
  }
}
