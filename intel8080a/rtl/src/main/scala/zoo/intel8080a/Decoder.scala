package zoo.intel8080a

import chisel3._
import chisel3.util._

class Intel8080aCtrlSignals extends Bundle {
  val is_lda     = Bool()
  val is_sta     = Bool()
  val is_mov_b_a = Bool()
  val is_add_b   = Bool()
  val is_hlt     = Bool()
  val legal    = Bool()
}

class Intel8080aDecoder extends Module {
  val io = IO(new Bundle {
    val opcode = Input(UInt(8.W))
    val ctrl   = Output(new Intel8080aCtrlSignals)
  })

  // Defaults
  io.ctrl.is_lda     := false.B
  io.ctrl.is_sta     := false.B
  io.ctrl.is_mov_b_a := false.B
  io.ctrl.is_add_b   := false.B
  io.ctrl.is_hlt     := false.B

  io.ctrl.legal := false.B
  switch(io.opcode) {
    is(0x3A.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_lda     := true.B } // LDA addr
    is(0x32.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_sta     := true.B } // STA addr
    is(0x47.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_mov_b_a := true.B } // MOV B, A (A to B)
    is(0x80.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_add_b   := true.B } // ADD B
    is(0x76.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_hlt     := true.B } // HLT
  }
}
