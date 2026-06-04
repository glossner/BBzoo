package zoo.cambridgeedsac

import chisel3._
import chisel3.util._

class CambridgeedsacCtrlSignals extends Bundle {
  val mem_read  = Bool()
  val mem_write = Bool()
  val is_add    = Bool()
  val is_sub    = Bool()
  val is_t      = Bool() // Store & clear
  val is_u      = Bool() // Store & no-clear
  val is_z      = Bool() // Halt
}

class CambridgeedsacDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(17.W))
    val ctrl = Output(new CambridgeedsacCtrlSignals)
    val addr = Output(UInt(10.W))
  })

  // Format: opcode (bits 10-16), address (bits 0-9)
  val opcode = io.inst(16, 10)
  io.addr   := io.inst(9, 0)

  // Defaults
  io.ctrl.mem_read  := false.B
  io.ctrl.mem_write := false.B
  io.ctrl.is_add    := false.B
  io.ctrl.is_sub    := false.B
  io.ctrl.is_t      := false.B
  io.ctrl.is_u      := false.B
  io.ctrl.is_z      := false.B

  switch(opcode) {
    is(1.U) { // A
      io.ctrl.mem_read := true.B
      io.ctrl.is_add   := true.B
    }
    is(2.U) { // S
      io.ctrl.mem_read := true.B
      io.ctrl.is_sub   := true.B
    }
    is(3.U) { // T
      io.ctrl.mem_write := true.B
      io.ctrl.is_t      := true.B
    }
    is(4.U) { // U
      io.ctrl.mem_write := true.B
      io.ctrl.is_u      := true.B
    }
    is(5.U) { // Z
      io.ctrl.is_z      := true.B
    }
  }
}
