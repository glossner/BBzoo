package zoo.motorola6800

import chisel3._
import chisel3.util._

class Motorola6800CtrlSignals extends Bundle {
  val is_ldaa = Bool()
  val is_staa = Bool()
  val is_adda = Bool()
  val is_wai  = Bool()
  val legal    = Bool()
}

class Motorola6800Decoder extends Module {
  val io = IO(new Bundle {
    val opcode = Input(UInt(8.W))
    val ctrl   = Output(new Motorola6800CtrlSignals)
  })

  // Defaults
  io.ctrl.is_ldaa := false.B
  io.ctrl.is_staa := false.B
  io.ctrl.is_adda := false.B
  io.ctrl.is_wai  := false.B

  io.ctrl.legal := false.B
  switch(io.opcode) {
    is(0xB6.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_ldaa := true.B } // LDAA extended
    is(0xB7.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_staa := true.B } // STAA extended
    is(0xBB.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_adda := true.B } // ADDA extended
    is(0x3E.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_wai  := true.B } // WAI
  }
}
