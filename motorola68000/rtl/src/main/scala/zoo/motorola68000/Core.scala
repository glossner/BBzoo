package zoo.motorola68000

import chisel3._
import chisel3.util._
import zoo.common.components._

class M68kCore extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(32, 16) // 32-bit address, 16-bit data bus
    
    // Debug ports
    val debug_regs = Output(Vec(16, UInt(32.W)))
    val debug_pc = Output(UInt(32.W))
    val debug_state = Output(UInt(4.W))
  })
  
  // 1. Decoder
  val decoder = Module(new M68kDecoder)
  val inst_reg = RegInit(0.U(16.W))
  decoder.io.inst := inst_reg

  // 2. Register File (16 registers: D0-D7 are 0-7, A0-A7 are 8-15)
  val regFile = Module(new GeneralRegFile(16, 32))
  
  // Map src/dest from instruction to register file index
  val src_reg_idx = Wire(UInt(4.W))
  src_reg_idx := Mux(decoder.io.src_mode === 0.U, decoder.io.src_reg, decoder.io.src_reg + 8.U)

  val dest_reg_idx = Wire(UInt(4.W))
  dest_reg_idx := Mux(decoder.io.dest_mode === 0.U, decoder.io.dest_reg, decoder.io.dest_reg + 8.U)

  regFile.io.rs1_addr := src_reg_idx
  regFile.io.rs2_addr := dest_reg_idx

  // 3. ALU
  val alu = Module(new ParameterizedALU(32))
  
  val pc = RegInit(0.U(32.W))
  val imm = RegInit(0.U(32.W))
  val rdata_buf = RegInit(0.U(32.W))
  val result_reg = RegInit(0.U(32.W))
  val dest_addr_reg = RegInit(0.U(32.W))

  // State Machine States
  val sRESET :: sFETCH_OP :: sDECODE :: sFETCH_IMM1 :: sFETCH_IMM2 :: sREAD_MEM1 :: sREAD_MEM2 :: sEXECUTE :: sWRITE_MEM1 :: sWRITE_MEM2 :: sWRITEBACK :: Nil = Enum(11)
  val state = RegInit(sRESET)

  // Default Memory Signals
  io.mem.req := false.B
  io.mem.addr := 0.U
  io.mem.write := false.B
  io.mem.wdata := 0.U

  // Default Register File Write Signals
  regFile.io.rd_addr := 0.U
  regFile.io.rd_data := 0.U
  regFile.io.wen := false.B

  // ALU Connections
  val a_val = Wire(UInt(32.W))
  val b_val = Wire(UInt(32.W))
  val alu_op = Wire(UInt(4.W))

  a_val := regFile.io.rs2_data
  b_val := MuxCase(0.U(32.W), Seq(
    (decoder.io.src_mode === 3.U) -> imm,
    (decoder.io.src_mode === 2.U) -> rdata_buf,
    (decoder.io.src_mode === 0.U || decoder.io.src_mode === 1.U) -> regFile.io.rs1_data
  ))

  alu_op := AluOp.PASS_B
  when(decoder.io.is_add) {
    alu_op := AluOp.ADD
  }.elsewhen(decoder.io.is_sub) {
    alu_op := AluOp.SUB
  }

  alu.io.a := a_val
  alu.io.b := b_val
  alu.io.op := alu_op

  // State Machine logic
  switch(state) {
    is(sRESET) {
      pc := 0.U
      state := sFETCH_OP
    }
    
    is(sFETCH_OP) {
      io.mem.req := true.B
      io.mem.addr := pc
      io.mem.write := false.B
      when(io.mem.ready) {
        inst_reg := io.mem.rdata
        state := sDECODE
      }
    }
    
    is(sDECODE) {
      when(decoder.io.is_bra) {
        // Unconditional branch is executed immediately
        val next_pc = pc + 2.U
        val offset_extended = Wire(SInt(32.W))
        offset_extended := decoder.io.bra_offset
        pc := (next_pc.asSInt + offset_extended).asUInt
        state := sFETCH_OP
      }.elsewhen(decoder.io.src_mode === 3.U) {
        state := sFETCH_IMM1
      }.elsewhen(decoder.io.src_mode === 2.U) {
        state := sREAD_MEM1
      }.otherwise {
        state := sEXECUTE
      }
    }
    
    is(sFETCH_IMM1) {
      io.mem.req := true.B
      io.mem.addr := pc + 2.U
      io.mem.write := false.B
      when(io.mem.ready) {
        imm := Cat(io.mem.rdata, 0.U(16.W))
        state := sFETCH_IMM2
      }
    }
    
    is(sFETCH_IMM2) {
      io.mem.req := true.B
      io.mem.addr := pc + 4.U
      io.mem.write := false.B
      when(io.mem.ready) {
        imm := Cat(imm(31, 16), io.mem.rdata)
        state := sEXECUTE
      }
    }
    
    is(sREAD_MEM1) {
      io.mem.req := true.B
      io.mem.addr := regFile.io.rs1_data
      io.mem.write := false.B
      when(io.mem.ready) {
        rdata_buf := Cat(io.mem.rdata, 0.U(16.W))
        state := sREAD_MEM2
      }
    }
    
    is(sREAD_MEM2) {
      io.mem.req := true.B
      io.mem.addr := regFile.io.rs1_data + 2.U
      io.mem.write := false.B
      when(io.mem.ready) {
        rdata_buf := Cat(rdata_buf(31, 16), io.mem.rdata)
        state := sEXECUTE
      }
    }
    
    is(sEXECUTE) {
      result_reg := alu.io.out
      when(decoder.io.dest_mode === 2.U) {
        dest_addr_reg := regFile.io.rs2_data
        state := sWRITE_MEM1
      }.otherwise {
        state := sWRITEBACK
      }
    }
    
    is(sWRITE_MEM1) {
      io.mem.req := true.B
      io.mem.addr := dest_addr_reg
      io.mem.write := true.B
      io.mem.wdata := result_reg(31, 16)
      when(io.mem.ready) {
        state := sWRITE_MEM2
      }
    }
    
    is(sWRITE_MEM2) {
      io.mem.req := true.B
      io.mem.addr := dest_addr_reg + 2.U
      io.mem.write := true.B
      io.mem.wdata := result_reg(15, 0)
      when(io.mem.ready) {
        pc := pc + Cat(0.U(30.W), decoder.io.inst_words) * 2.U
        state := sFETCH_OP
      }
    }
    
    is(sWRITEBACK) {
      regFile.io.wen := true.B
      regFile.io.rd_addr := dest_reg_idx
      regFile.io.rd_data := result_reg
      pc := pc + Cat(0.U(30.W), decoder.io.inst_words) * 2.U
      state := sFETCH_OP
    }
  }

  // Debug Outputs
  io.debug_regs := regFile.io.regs_debug
  io.debug_pc := pc
  io.debug_state := state
}
