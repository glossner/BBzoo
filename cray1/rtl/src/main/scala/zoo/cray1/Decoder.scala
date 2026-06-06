package zoo.cray1

import chisel3._
import chisel3.util._

class Cray1CtrlSignals extends Bundle {
  val is_add_a   = Bool() // A(i) := A(j) + A(k)
  val is_add_s   = Bool() // S(i) := S(j) + S(k)
  val is_add_v   = Bool() // V(i) := V(j) + V(k)
  val is_add_vs  = Bool() // V(i) := V(j) + S(k)
  
  val is_load_a  = Bool() // A(i) := mem[A(j)]
  val is_store_a = Bool() // mem[A(j)] := A(i)
  
  val is_load_v  = Bool() // V(i) := mem[A(j)]
  val is_store_v = Bool() // mem[A(j)] := V(i)
  
  val is_set_vl  = Bool() // VL := A(j)
  val is_li_a    = Bool() // A(i) := immediate (inst(11,0))
  val is_hlt     = Bool() // Halt processor
  val legal    = Bool()
}

/**
 * Cray-1 Instruction Decoder.
 * Decodes the 16-bit instruction parcel into control signals.
 * Instruction fields:
 * - g: bits 15-12 (4-bit primary opcode)
 * - h: bits 11-9 (3-bit sub-opcode)
 * - i: bits 8-6 (3-bit destination register)
 * - j: bits 5-3 (3-bit first source register)
 * - k: bits 2-0 (3-bit second source register)
 */
class Cray1Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(16.W))
    val ctrl = Output(new Cray1CtrlSignals)
    
    // Extracted register fields
    val r_i  = Output(UInt(3.W))
    val r_j  = Output(UInt(3.W))
    val r_k  = Output(UInt(3.W))
  })

  val g = io.inst(15, 12)
  val h = io.inst(11, 9)
  
  io.r_i := io.inst(8, 6)
  io.r_j := io.inst(5, 3)
  io.r_k := io.inst(2, 0)

  // Default controls
  io.ctrl.is_add_a   := false.B
  io.ctrl.is_add_s   := false.B
  io.ctrl.is_add_v   := false.B
  io.ctrl.is_add_vs  := false.B
  io.ctrl.is_load_a  := false.B
  io.ctrl.is_store_a := false.B
  io.ctrl.is_load_v  := false.B
  io.ctrl.is_store_v := false.B
  io.ctrl.is_set_vl  := false.B
  io.ctrl.is_li_a    := false.B
  io.ctrl.is_hlt     := false.B

  io.ctrl.legal := false.B
  switch(g) {
    is(0.U) {
      io.ctrl.legal := true.B
      
      switch(h) {
        is(0.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_set_vl := true.B }
        is(1.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_hlt := true.B }
      }
    }
    is(1.U) {
      io.ctrl.legal := true.B
      
      switch(h) {
        is(0.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_load_a := true.B }
        is(1.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_store_a := true.B }
      }
    }
    is(2.U) {
      io.ctrl.legal := true.B
      
      switch(h) {
        is(0.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_load_v := true.B }
        is(1.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_store_v := true.B }
      }
    }
    is(3.U) {
      io.ctrl.legal := true.B
      
      switch(h) {
        is(0.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_add_a := true.B }
        is(1.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_add_s := true.B }
        is(2.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_add_v := true.B }
        is(3.U) {
      io.ctrl.legal := true.B
       io.ctrl.is_add_vs := true.B }
      }
    }
    is(4.U) {
      io.ctrl.legal := true.B
      
      io.ctrl.is_li_a := true.B
    }
  }
}
