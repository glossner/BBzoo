package zoo.ibm650

import chisel3._
import chisel3.util._

class Ibm650CtrlSignals extends Bundle {
  val mem_read  = Bool()
  val mem_write = Bool()
  val is_ld     = Bool()
  val is_add    = Bool()
  val is_st     = Bool()
  val is_hlt    = Bool()
}

class Ibm650Decoder extends Module {
  val io = IO(new Bundle {
    val inst      = Input(UInt(40.W))
    val ctrl      = Output(new Ibm650CtrlSignals)
    val data_addr = Output(UInt(16.W))
    val next_addr = Output(UInt(16.W))
  })

  // Format: opcode (39-32), data_addr (31-16), next_addr (15-0)
  val opcode = io.inst(39, 32)
  io.data_addr := io.inst(31, 16)
  io.next_addr := io.inst(15, 0)

  // Defaults
  io.ctrl.mem_read  := false.B
  io.ctrl.mem_write := false.B
  io.ctrl.is_ld     := false.B
  io.ctrl.is_add    := false.B
  io.ctrl.is_st     := false.B
  io.ctrl.is_hlt    := false.B

  switch(opcode) {
    is(1.U) { // LD
      io.ctrl.mem_read := true.B
      io.ctrl.is_ld    := true.B
    }
    is(2.U) { // ADD
      io.ctrl.mem_read := true.B
      io.ctrl.is_add   := true.B
    }
    is(3.U) { // ST
      io.ctrl.mem_write := true.B
      io.ctrl.is_st     := true.B
    }
    is(4.U) { // HLT
      io.ctrl.is_hlt    := true.B
    }
  }
}
