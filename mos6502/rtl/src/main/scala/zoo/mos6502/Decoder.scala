package zoo.mos6502

import chisel3._
import chisel3.util._

class Mos6502CtrlSignals extends Bundle {
  val is_brk  = Bool()
  val is_clc  = Bool()
  val lda_imm = Bool()
  val lda_abs = Bool()
  val sta_abs = Bool()
  val adc_imm = Bool()
  val adc_abs = Bool()
  val legal    = Bool()
}

class Mos6502Decoder extends Module {
  val io = IO(new Bundle {
    val opcode = Input(UInt(8.W))
    val ctrl   = Output(new Mos6502CtrlSignals)
  })

  // Defaults
  io.ctrl.is_brk  := false.B
  io.ctrl.is_clc  := false.B
  io.ctrl.lda_imm := false.B
  io.ctrl.lda_abs := false.B
  io.ctrl.sta_abs := false.B
  io.ctrl.adc_imm := false.B
  io.ctrl.adc_abs := false.B

  io.ctrl.legal := false.B
  switch(io.opcode) {
    is(0x00.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_brk  := true.B }
    is(0x18.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_clc  := true.B }
    is(0xA9.U) {
      io.ctrl.legal := true.B
       io.ctrl.lda_imm := true.B }
    is(0xAD.U) {
      io.ctrl.legal := true.B
       io.ctrl.lda_abs := true.B }
    is(0x8D.U) {
      io.ctrl.legal := true.B
       io.ctrl.sta_abs := true.B }
    is(0x69.U) {
      io.ctrl.legal := true.B
       io.ctrl.adc_imm := true.B }
    is(0x6D.U) {
      io.ctrl.legal := true.B
       io.ctrl.adc_abs := true.B }
  }
}
