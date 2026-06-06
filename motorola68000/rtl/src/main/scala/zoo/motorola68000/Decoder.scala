package zoo.motorola68000

import chisel3._
import chisel3.util._

class M68kDecoder extends Module {
  val io = IO(new Bundle {
    val legal = Output(Bool())

    val inst = Input(UInt(16.W))
    
    // Decoded control signals
    val is_move      = Output(Bool())
    val is_movea     = Output(Bool())
    val is_add       = Output(Bool())
    val is_sub       = Output(Bool())
    val is_bra       = Output(Bool())
    
    // Operand types/modes: 0=Dn, 1=An, 2=(An), 3=Immediate
    val dest_mode    = Output(UInt(2.W)) 
    val dest_reg     = Output(UInt(3.W))
    val src_mode     = Output(UInt(2.W))
    val src_reg      = Output(UInt(3.W))
    
    // Branch offset (8-bit signed)
    val bra_offset   = Output(SInt(8.W))
    
    // Instruction length in 16-bit words (1, 2, or 3)
    val inst_words   = Output(UInt(2.W))
  })
  
  // Default outputs
  io.is_move    := false.B
  io.is_movea   := false.B
  io.is_add     := false.B
  io.is_sub     := false.B
  io.is_bra     := false.B
  io.dest_mode  := 0.U
  io.dest_reg   := 0.U
  io.src_mode   := 0.U
  io.src_reg    := 0.U
  io.bra_offset := 0.S
  io.inst_words := 1.U
  
  val op = io.inst(15, 12)
  
  when(op === 0x2.U) { // MOVE.L or MOVEA.L (32-bit longword moves)
    val dst_reg  = io.inst(11, 9)
    val dst_mode = io.inst(8, 6)
    val src_mode = io.inst(5, 3)
    val src_reg  = io.inst(2, 0)
    
    when(dst_mode === "b001".U) { // MOVEA.L (destination is An)
      io.is_movea  := true.B
      io.dest_mode := 1.U
      io.dest_reg  := dst_reg
    }.elsewhen(dst_mode === "b010".U) { // MOVE.L to (An) (destination is indirect)
      io.is_move   := true.B
      io.dest_mode := 2.U
      io.dest_reg  := dst_reg
    }.otherwise { // MOVE.L to Dn (destination is Dn)
      io.is_move   := true.B
      io.dest_mode := 0.U
      io.dest_reg  := dst_reg
    }
    
    // Decode source operand
    when(src_mode === "b111".U && src_reg === "b100".U) {
      io.src_mode   := 3.U // Immediate
      io.inst_words := 3.U // 1 opcode word + 2 immediate words
    }.elsewhen(src_mode === "b010".U) {
      io.src_mode   := 2.U // Address register indirect (An)
      io.src_reg    := src_reg
    }.elsewhen(src_mode === "b001".U) {
      io.src_mode   := 1.U // Address register direct
      io.src_reg    := src_reg
    }.otherwise {
      io.src_mode   := 0.U // Data register direct
      io.src_reg    := src_reg
    }
  }
  .elsewhen(op === 0xD.U && io.inst(8, 6) === "b010".U) { // ADD.L Dm, Dn
    io.is_add    := true.B
    io.dest_mode := 0.U
    io.dest_reg  := io.inst(11, 9)
    io.src_mode  := 0.U
    io.src_reg   := io.inst(2, 0)
  }
  .elsewhen(op === 0x9.U && io.inst(8, 6) === "b010".U) { // SUB.L Dm, Dn
    io.is_sub    := true.B
    io.dest_mode := 0.U
    io.dest_reg  := io.inst(11, 9)
    io.src_mode  := 0.U
    io.src_reg   := io.inst(2, 0)
  }
  .elsewhen(io.inst(15, 8) === 0x60.U) { // BRA (8-bit displacement)
    io.is_bra      := true.B
    io.bra_offset  := io.inst(7, 0).asSInt
    io.inst_words  := 1.U
  }

  io.legal := (op === 0.U || op === 2.U || op === 9.U || op === 13.U)
}
