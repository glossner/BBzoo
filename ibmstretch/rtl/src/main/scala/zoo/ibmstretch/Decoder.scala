package zoo.ibmstretch

import chisel3._
import chisel3.util._

class IbmstretchCtrlSignals extends Bundle {
  val mem_read  = Bool()
  val mem_write = Bool()
  val is_ld     = Bool()
  val is_add    = Bool()
  val is_st     = Bool()
  val is_ldx    = Bool()
  val is_addx   = Bool()
  val is_hlt    = Bool()
}

class IbmstretchDecoder extends Module {
  val io = IO(new Bundle {
    val inst      = Input(UInt(64.W))
    val ctrl      = Output(new IbmstretchCtrlSignals)
    val index_reg = Output(UInt(4.W))
    val addr      = Output(UInt(20.W))
  })

  // Format: opcode (63-56), index_reg (55-52), address (19-0)
  val opcode = io.inst(63, 56)
  io.index_reg := io.inst(55, 52)
  io.addr      := io.inst(19, 0)

  // Defaults
  io.ctrl.mem_read  := false.B
  io.ctrl.mem_write := false.B
  io.ctrl.is_ld     := false.B
  io.ctrl.is_add    := false.B
  io.ctrl.is_st     := false.B
  io.ctrl.is_ldx    := false.B
  io.ctrl.is_addx   := false.B
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
    is(4.U) { // LDX
      io.ctrl.is_ldx   := true.B
    }
    is(5.U) { // ADDX
      io.ctrl.is_addx  := true.B
    }
    is(6.U) { // HLT
      io.ctrl.is_hlt   := true.B
    }
  }
}
