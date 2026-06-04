package zoo.ibm360

import chisel3._
import chisel3.util._
import zoo.common.components.AluOp

class Ibm360CtrlSignals extends Bundle {
  val is_rr      = Bool()
  val is_rx      = Bool()
  val mem_read   = Bool()
  val mem_write  = Bool()
  val rf_wen     = Bool()
  val alu_op     = UInt(4.W)
  val inst_len   = UInt(3.W) // 2 for RR, 4 for RX, etc.
  val is_branch  = Bool()
  val rf_src_mem = Bool()
}

/**
 * IBM System/360 Decoder.
 * Handles RR (Register-Register) and RX (Register-Index-Memory) instructions.
 */
class Ibm360Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(48.W)) // Up to 48 bits, right-aligned or left-aligned?
    // Let's assume inst is left-aligned in a 48-bit register, so:
    // Byte 0 (bits 47-40) is the Opcode.
    val ctrl = Output(new Ibm360CtrlSignals)
    
    // Extracted instruction fields
    val r1   = Output(UInt(4.W))
    val r2   = Output(UInt(4.W))
    val x2   = Output(UInt(4.W))
    val b2   = Output(UInt(4.W))
    val d2   = Output(UInt(12.W))
  })

  val opcode = io.inst(47, 40)

  // Extracted fields defaults
  io.r1 := io.inst(39, 36)
  io.r2 := io.inst(35, 32)
  io.x2 := io.inst(35, 32)
  io.b2 := io.inst(31, 28)
  io.d2 := io.inst(27, 16)

  // Default control signals
  io.ctrl.is_rr     := false.B
  io.ctrl.is_rx     := false.B
  io.ctrl.mem_read  := false.B
  io.ctrl.mem_write := false.B
  io.ctrl.rf_wen    := false.B
  io.ctrl.alu_op    := AluOp.PASS_A
  io.ctrl.inst_len  := 2.U
  io.ctrl.is_branch := false.B
  io.ctrl.rf_src_mem := false.B

  // Decode logic based on top bits of opcode
  // In System/360:
  // - 00xxxxxx: RR format (2 bytes)
  // - 01xxxxxx: RX format (4 bytes)
  // - 10xxxxxx: RS or SI format (4 bytes)
  // - 11xxxxxx: SS format (6 bytes)
  val opcode_type = opcode(7, 6)

  switch(opcode_type) {
    is(0.U) { // RR format (16-bit)
      io.ctrl.is_rr    := true.B
      io.ctrl.inst_len := 2.U
      
      switch(opcode) {
        is(0x18.U) { // LR (Load Register)
          io.ctrl.rf_wen := true.B
          io.ctrl.alu_op := AluOp.PASS_B
        }
        is(0x1A.U) { // AR (Add Register)
          io.ctrl.rf_wen := true.B
          io.ctrl.alu_op := AluOp.ADD
        }
        is(0x1B.U) { // SR (Subtract Register)
          io.ctrl.rf_wen := true.B
          io.ctrl.alu_op := AluOp.SUB
        }
        is(0x19.U) { // CR (Compare Register)
          io.ctrl.alu_op := AluOp.SUB
        }
      }
    }
    
    is(1.U) { // RX format (32-bit)
      io.ctrl.is_rx    := true.B
      io.ctrl.inst_len := 4.U
      
      switch(opcode) {
        is(0x58.U) { // L (Load)
          io.ctrl.mem_read   := true.B
          io.ctrl.rf_wen     := true.B
          io.ctrl.alu_op     := AluOp.PASS_B
          io.ctrl.rf_src_mem := true.B
        }
        is(0x5A.U) { // A (Add)
          io.ctrl.mem_read := true.B
          io.ctrl.rf_wen   := true.B
          io.ctrl.alu_op   := AluOp.ADD
        }
        is(0x5B.U) { // S (Subtract)
          io.ctrl.mem_read := true.B
          io.ctrl.rf_wen   := true.B
          io.ctrl.alu_op   := AluOp.SUB
        }
        is(0x50.U) { // ST (Store)
          io.ctrl.mem_write := true.B
        }
        is(0x47.U) { // BC (Branch on Condition)
          io.ctrl.is_branch := true.B
        }
      }
    }
  }
}
