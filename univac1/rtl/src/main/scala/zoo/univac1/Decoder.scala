package zoo.univac1

import chisel3._
import chisel3.util._

class Univac1CtrlSignals extends Bundle {
  val mem_read  = Bool()
  val mem_write = Bool()
  val is_b      = Bool()
  val is_h      = Bool()
  val is_a      = Bool()
  val is_s      = Bool()
  val is_q      = Bool()
  val legal    = Bool()
}

class Univac1Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(72.W))
    val ctrl = Output(new Univac1CtrlSignals)
    val addr = Output(UInt(16.W))
  })

  // Format: opcode is at bits 64-71, address is at bits 0-15
  val opcode = io.inst(71, 64)
  io.addr := io.inst(15, 0)

  // Defaults
  io.ctrl.mem_read  := false.B
  io.ctrl.mem_write := false.B
  io.ctrl.is_b      := false.B
  io.ctrl.is_h      := false.B
  io.ctrl.is_a      := false.B
  io.ctrl.is_s      := false.B
  io.ctrl.is_q      := false.B

  io.ctrl.legal := false.B
  switch(opcode) {
    is(1.U) {
      io.ctrl.legal := true.B
       // B
      io.ctrl.mem_read := true.B
      io.ctrl.is_b     := true.B
    }
    is(2.U) {
      io.ctrl.legal := true.B
       // H
      io.ctrl.mem_write := true.B
      io.ctrl.is_h      := true.B
    }
    is(3.U) {
      io.ctrl.legal := true.B
       // A
      io.ctrl.mem_read := true.B
      io.ctrl.is_a     := true.B
    }
    is(4.U) {
      io.ctrl.legal := true.B
       // S
      io.ctrl.mem_read := true.B
      io.ctrl.is_s     := true.B
    }
    is(5.U) {
      io.ctrl.legal := true.B
       // Q
      io.ctrl.is_q     := true.B
    }
  }
}
