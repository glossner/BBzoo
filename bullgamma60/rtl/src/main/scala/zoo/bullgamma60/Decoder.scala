package zoo.bullgamma60

import chisel3._
import chisel3.util._

class Bullgamma60CtrlSignals extends Bundle {
  val mem_read  = Bool()
  val mem_write = Bool()
  val is_ld     = Bool()
  val is_add    = Bool()
  val is_st     = Bool()
  val is_fork   = Bool()
  val is_join   = Bool()
  val is_hlt    = Bool()
  val legal    = Bool()
}

class Bullgamma60Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(24.W))
    val ctrl = Output(new Bullgamma60CtrlSignals)
    val reg  = Output(UInt(2.W))
    val addr = Output(UInt(16.W))
  })

  val opcode = io.inst(23, 18)
  io.reg    := io.inst(17, 16)
  io.addr   := io.inst(15, 0)

  // Defaults
  io.ctrl.mem_read  := false.B
  io.ctrl.mem_write := false.B
  io.ctrl.is_ld     := false.B
  io.ctrl.is_add    := false.B
  io.ctrl.is_st     := false.B
  io.ctrl.is_fork   := false.B
  io.ctrl.is_join   := false.B
  io.ctrl.is_hlt    := false.B

  io.ctrl.legal := false.B
  switch(opcode) {
    is(1.U) {
      io.ctrl.legal := true.B
       // LD
      io.ctrl.mem_read := true.B
      io.ctrl.is_ld    := true.B
    }
    is(2.U) {
      io.ctrl.legal := true.B
       // ADD
      io.ctrl.mem_read := true.B
      io.ctrl.is_add   := true.B
    }
    is(3.U) {
      io.ctrl.legal := true.B
       // ST
      io.ctrl.mem_write := true.B
      io.ctrl.is_st     := true.B
    }
    is(4.U) {
      io.ctrl.legal := true.B
       // FORK
      io.ctrl.is_fork  := true.B
    }
    is(5.U) {
      io.ctrl.legal := true.B
       // JOIN
      io.ctrl.is_join  := true.B
    }
    is(6.U) {
      io.ctrl.legal := true.B
       // HLT
      io.ctrl.is_hlt   := true.B
    }
  }
}
