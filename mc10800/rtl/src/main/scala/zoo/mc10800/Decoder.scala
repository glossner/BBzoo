package zoo.mc10800

import chisel3._
import chisel3.util._
import zoo.common.components.AluOp

class Mc10800CtrlSignals extends Bundle {
  val mem_read  = Bool()
  val mem_write = Bool()
  val wen       = Bool()
  val alu_op    = UInt(4.W)
  val is_jnz    = Bool()
  val is_jmp    = Bool()
  val is_hlt    = Bool()
}

class Mc10800Decoder extends Module {
  val io = IO(new Bundle {
    val inst     = Input(UInt(16.W))
    val ctrl     = Output(new Mc10800CtrlSignals)
    val rd_addr  = Output(UInt(4.W))
    val rs1_addr = Output(UInt(4.W))
    val rs2_addr = Output(UInt(4.W))
  })

  val opcode = io.inst(15, 12)
  io.rd_addr  := io.inst(11, 8)
  io.rs1_addr := io.inst(7, 4)
  io.rs2_addr := io.inst(3, 0)

  // Default values
  io.ctrl.mem_read  := false.B
  io.ctrl.mem_write := false.B
  io.ctrl.wen       := false.B
  io.ctrl.alu_op    := AluOp.PASS_A
  io.ctrl.is_jnz    := false.B
  io.ctrl.is_jmp    := false.B
  io.ctrl.is_hlt    := false.B

  switch(opcode) {
    is(0.U) { // HALT / HLT
      io.ctrl.is_hlt := true.B
    }
    is(1.U) { // LD Rd, addr
      io.ctrl.mem_read := true.B
      io.ctrl.wen      := true.B
    }
    is(2.U) { // ST Rs, addr
      io.ctrl.mem_write := true.B
      io.rs1_addr := io.inst(11, 8)
    }
    is(3.U) { // ADD Rd, Ra, Rb
      io.ctrl.alu_op := AluOp.ADD
      io.ctrl.wen    := true.B
    }
    is(4.U) { // SUB Rd, Ra, Rb
      io.ctrl.alu_op := AluOp.SUB
      io.ctrl.wen    := true.B
    }
    is(5.U) { // AND Rd, Ra, Rb
      io.ctrl.alu_op := AluOp.AND
      io.ctrl.wen    := true.B
    }
    is(6.U) { // OR Rd, Ra, Rb
      io.ctrl.alu_op := AluOp.OR
      io.ctrl.wen    := true.B
    }
    is(7.U) { // JNZ Rd, addr
      io.ctrl.is_jnz := true.B
      io.rs1_addr    := io.inst(11, 8) // Read Rd to check if zero
    }
    is(8.U) { // JMP addr
      io.ctrl.is_jmp := true.B
    }
    is(9.U) { // LDI Rd, Rs
      io.ctrl.mem_read := true.B
      io.ctrl.wen      := true.B
      io.rs1_addr      := io.inst(7, 4) // Rs is source address register
    }
    is(10.U) { // STI Rs, Rd
      io.ctrl.mem_write := true.B
      io.rs1_addr       := io.inst(11, 8) // Rs is source data register
      io.rs2_addr       := io.inst(7, 4)  // Rd is destination address register
    }
  }
}
