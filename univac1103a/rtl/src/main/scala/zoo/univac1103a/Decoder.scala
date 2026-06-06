package zoo.univac1103a

import chisel3._
import chisel3.util._

class Univac1103aCtrlSignals extends Bundle {
  val mem_read  = Bool()
  val mem_write = Bool()
  val is_tp     = Bool()
  val is_add    = Bool()
  val is_sub    = Bool()
  val is_hlt    = Bool()
  val legal    = Bool()
}

class Univac1103aDecoder extends Module {
  val io = IO(new Bundle {
    val inst   = Input(UInt(36.W))
    val ctrl   = Output(new Univac1103aCtrlSignals)
    val u_addr = Output(UInt(15.W))
    val v_addr = Output(UInt(15.W))
  })

  // Format: opcode (35-30), u_addr (29-15), v_addr (14-0)
  val opcode = io.inst(35, 30)
  io.u_addr := io.inst(29, 15)
  io.v_addr := io.inst(14, 0)

  // Defaults
  io.ctrl.mem_read  := false.B
  io.ctrl.mem_write := false.B
  io.ctrl.is_tp     := false.B
  io.ctrl.is_add    := false.B
  io.ctrl.is_sub    := false.B
  io.ctrl.is_hlt    := false.B

  io.ctrl.legal := false.B
  switch(opcode) {
    is(11.U) {
      io.ctrl.legal := true.B
       // TP
      io.ctrl.mem_read := true.B
      io.ctrl.is_tp    := true.B
    }
    is(12.U) {
      io.ctrl.legal := true.B
       // ADD
      io.ctrl.mem_read := true.B
      io.ctrl.is_add   := true.B
    }
    is(13.U) {
      io.ctrl.legal := true.B
       // SUB
      io.ctrl.mem_read := true.B
      io.ctrl.is_sub   := true.B
    }
    is(14.U) {
      io.ctrl.legal := true.B
       // HLT
      io.ctrl.is_hlt   := true.B
    }
  }
}
