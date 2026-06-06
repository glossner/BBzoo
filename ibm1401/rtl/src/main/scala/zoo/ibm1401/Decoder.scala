package zoo.ibm1401

import chisel3._
import chisel3.util._

class Ibm1401CtrlSignals extends Bundle {
  val is_mc  = Bool()
  val is_add = Bool()
  val is_sub = Bool()
  val is_hlt = Bool()
  val legal    = Bool()
}

class Ibm1401Decoder extends Module {
  val io = IO(new Bundle {
    val inst   = Input(UInt(36.W))
    val ctrl   = Output(new Ibm1401CtrlSignals)
    val a_addr = Output(UInt(12.W))
    val b_addr = Output(UInt(12.W))
  })

  // Format: opcode (31-24), a_addr (23-12), b_addr (11-0)
  val opcode = io.inst(31, 24)
  io.a_addr  := io.inst(23, 12)
  io.b_addr  := io.inst(11, 0)

  // Defaults
  io.ctrl.is_mc  := false.B
  io.ctrl.is_add := false.B
  io.ctrl.is_sub := false.B
  io.ctrl.is_hlt := false.B

  io.ctrl.legal := false.B
  switch(opcode) {
    is(1.U) {
      io.ctrl.legal := true.B
       // MC
      io.ctrl.is_mc := true.B
    }
    is(2.U) {
      io.ctrl.legal := true.B
       // A
      io.ctrl.is_add := true.B
    }
    is(3.U) {
      io.ctrl.legal := true.B
       // S
      io.ctrl.is_sub := true.B
    }
    is(4.U) {
      io.ctrl.legal := true.B
       // HLT
      io.ctrl.is_hlt := true.B
    }
  }
}
