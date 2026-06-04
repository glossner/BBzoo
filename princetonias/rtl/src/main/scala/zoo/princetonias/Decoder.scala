package zoo.princetonias

import chisel3._
import chisel3.util._

class PrincetoniasCtrlSignals extends Bundle {
  val mem_read  = Bool()
  val mem_write = Bool()
  val is_ld     = Bool()
  val is_st     = Bool()
  val is_add    = Bool()
  val is_sub    = Bool()
  val is_hlt    = Bool()
}

class PrincetoniasDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(40.W))
    val ctrl = Output(new PrincetoniasCtrlSignals)
    val addr = Output(UInt(12.W))
  })

  // Format: opcode is bits 12-39 (let's check bits 12-19 as primary opcode), address is bits 0-11
  val opcode = io.inst(19, 12)
  io.addr   := io.inst(11, 0)

  // Defaults
  io.ctrl.mem_read  := false.B
  io.ctrl.mem_write := false.B
  io.ctrl.is_ld     := false.B
  io.ctrl.is_st     := false.B
  io.ctrl.is_add    := false.B
  io.ctrl.is_sub    := false.B
  io.ctrl.is_hlt    := false.B

  switch(opcode) {
    is(1.U) { // LD
      io.ctrl.mem_read := true.B
      io.ctrl.is_ld    := true.B
    }
    is(2.U) { // ST
      io.ctrl.mem_write := true.B
      io.ctrl.is_st     := true.B
    }
    is(3.U) { // ADD
      io.ctrl.mem_read := true.B
      io.ctrl.is_add   := true.B
    }
    is(4.U) { // SUB
      io.ctrl.mem_read := true.B
      io.ctrl.is_sub   := true.B
    }
    is(5.U) { // HLT
      io.ctrl.is_hlt   := true.B
    }
  }
}
