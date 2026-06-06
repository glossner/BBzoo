package zoo.cdc6600ppu

import chisel3._
import chisel3.util._

class Cdc6600ppuCtrlSignals extends Bundle {
  val mem_read  = Bool()
  val mem_write = Bool()
  val is_ld     = Bool()
  val is_add    = Bool()
  val is_st     = Bool()
  val is_sub    = Bool()
  val is_hlt    = Bool()
  val legal    = Bool()
}

class Cdc6600ppuDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(12.W))
    val ctrl = Output(new Cdc6600ppuCtrlSignals)
    val addr = Output(UInt(6.W))
  })

  val opcode = io.inst(11, 6)
  io.addr   := io.inst(5, 0)

  // Defaults
  io.ctrl.mem_read  := false.B
  io.ctrl.mem_write := false.B
  io.ctrl.is_ld     := false.B
  io.ctrl.is_add    := false.B
  io.ctrl.is_st     := false.B
  io.ctrl.is_sub    := false.B
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
       // SUB
      io.ctrl.mem_read := true.B
      io.ctrl.is_sub   := true.B
    }
    is(5.U) {
      io.ctrl.legal := true.B
       // HLT
      io.ctrl.is_hlt   := true.B
    }
  }
}
