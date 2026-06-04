package zoo.manchestermu1

import chisel3._
import chisel3.util._

class Manchestermu1CtrlSignals extends Bundle {
  val mem_read  = Bool()
  val mem_write = Bool()
  val is_ldn    = Bool()
  val is_sub    = Bool()
  val is_jmp    = Bool()
  val is_jpr    = Bool()
  val is_stp    = Bool()
}

class Manchestermu1Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new Manchestermu1CtrlSignals)
    val addr = Output(UInt(13.W))
  })

  // Format: opcode (bits 15-13), addr (bits 12-0)
  val opcode = io.inst(15, 13)
  io.addr := io.inst(12, 0)

  // Defaults
  io.ctrl.mem_read  := false.B
  io.ctrl.mem_write := false.B
  io.ctrl.is_ldn    := false.B
  io.ctrl.is_sub    := false.B
  io.ctrl.is_jmp    := false.B
  io.ctrl.is_jpr    := false.B
  io.ctrl.is_stp    := false.B

  switch(opcode) {
    is(0.U) { // JMP
      io.ctrl.mem_read := true.B
      io.ctrl.is_jmp   := true.B
    }
    is(1.U) { // JPR
      io.ctrl.mem_read := true.B
      io.ctrl.is_jpr   := true.B
    }
    is(2.U) { // LDN
      io.ctrl.mem_read := true.B
      io.ctrl.is_ldn   := true.B
    }
    is(3.U) { // STO
      io.ctrl.mem_write := true.B
    }
    is(4.U) { // SUB
      io.ctrl.mem_read := true.B
      io.ctrl.is_sub   := true.B
    }
    is(7.U) { // STP
      io.ctrl.is_stp   := true.B
    }
  }
}
