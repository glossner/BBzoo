package zoo.decpdp8

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * DEC PDP-8 Core Processor.
 * A synthesizable 12-bit single-accumulator processor with a memory interface.
 */
class Pdp8Core extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 12, dataWidth = 12)
    val hlt = Output(Bool())
    
    // Debug outputs for testing/monitoring
    val pc_debug   = Output(UInt(12.W))
    val acc_debug  = Output(UInt(12.W))
    val link_debug = Output(Bool())
  })

  // Sub-modules
  val accReg  = Module(new AccumulatorReg(width = 12, hasLinkBit = true))
  val decoder = Module(new Pdp8Decoder)
  val alu     = Module(new ParameterizedALU(width = 12))

  // Core Registers
  val pc     = RegInit(0.U(12.W))
  val inst   = RegInit(0.U(12.W))
  val mar    = RegInit(0.U(12.W)) // Memory Address Register
  val md     = RegInit(0.U(12.W)) // Memory Data Register
  val hltReg = RegInit(false.B)

  io.hlt        := hltReg
  io.pc_debug   := pc
  io.acc_debug  := accReg.io.out_data
  io.link_debug := accReg.io.link_out

  // FSM States
  val sFETCH :: sDECODE :: sINDIRECT_REQ :: sEXECUTE_REQ :: sWRITEBACK :: Nil = Enum(5)
  val state = RegInit(sFETCH)

  // Default IO assignments
  io.mem.req   := false.B
  io.mem.addr  := mar
  io.mem.write := false.B
  io.mem.wdata := md

  // Decoder inputs
  decoder.io.inst     := inst
  decoder.io.acc_val  := accReg.io.out_data
  decoder.io.link_val := accReg.io.link_out

  // Accumulator inputs defaults
  accReg.io.in_data      := alu.io.out
  accReg.io.load         := false.B
  accReg.io.clear        := false.B
  accReg.io.complement   := false.B
  accReg.io.rotate_left  := false.B
  accReg.io.rotate_right := false.B
  accReg.io.link_in      := alu.io.carry_out
  accReg.io.link_load    := false.B
  accReg.io.link_comp    := false.B
  accReg.io.link_clear   := false.B

  // ALU inputs defaults
  alu.io.a  := accReg.io.out_data
  alu.io.b  := md
  alu.io.op := decoder.io.ctrl.alu_op

  // Address helper logic
  val page        = inst(7)
  val offset      = inst(6, 0)
  val direct_addr = Mux(page === 0.U, Cat(0.U(5.W), offset), Cat(pc(11, 7), offset))

  // FSM Logic
  switch(state) {
    is(sFETCH) {
      io.mem.req   := true.B
      io.mem.addr  := pc
      io.mem.write := false.B
      when(io.mem.ready) {
        inst  := io.mem.rdata
        pc    := pc + 1.U
        state := sDECODE
      }
    }
    
    is(sDECODE) {
      val indirect = inst(8)
      val op       = inst(11, 9)
      
      when(op === 7.U) { // OPR (operate) instruction
        val is_group2 = inst(8)
        when(!is_group2) {
          // Group 1
          accReg.io.clear      := decoder.io.ctrl.acc_clear
          accReg.io.link_clear := decoder.io.ctrl.link_clear
          accReg.io.complement := decoder.io.ctrl.acc_comp
          accReg.io.link_comp  := decoder.io.ctrl.link_comp
          accReg.io.rotate_left  := decoder.io.ctrl.acc_rot_l
          accReg.io.rotate_right := decoder.io.ctrl.acc_rot_r
          
          when(inst(0)) { // IAC (Increment ACC)
            alu.io.a            := accReg.io.out_data
            alu.io.b            := 1.U
            alu.io.op           := AluOp.ADD
            accReg.io.load      := true.B
            accReg.io.link_load := true.B
          }
        }.otherwise {
          // Group 2
          accReg.io.clear := decoder.io.ctrl.acc_clear
          when(decoder.io.skip) {
            pc := pc + 1.U // Skip next instruction
          }
          when(inst(1)) { // HLT
            hltReg := true.B
          }
        }
        state := sFETCH
      }.elsewhen(op === 6.U) { // IOT (input/output transfer) - treated as NOP
        state := sFETCH
      }.otherwise {
        // Memory Reference Instructions
        when(indirect) {
          mar   := direct_addr
          state := sINDIRECT_REQ
        }.otherwise {
          mar   := direct_addr
          state := sEXECUTE_REQ
        }
      }
    }
    
    is(sINDIRECT_REQ) {
      io.mem.req   := true.B
      io.mem.addr  := mar
      io.mem.write := false.B
      when(io.mem.ready) {
        mar   := io.mem.rdata // Target address fetched
        state := sEXECUTE_REQ
      }
    }
    
    is(sEXECUTE_REQ) {
      val op = inst(11, 9)
      switch(op) {
        is(0.U) { // AND
          io.mem.req   := true.B
          io.mem.addr  := mar
          io.mem.write := false.B
          when(io.mem.ready) {
            md    := io.mem.rdata
            state := sWRITEBACK
          }
        }
        is(1.U) { // TAD
          io.mem.req   := true.B
          io.mem.addr  := mar
          io.mem.write := false.B
          when(io.mem.ready) {
            md    := io.mem.rdata
            state := sWRITEBACK
          }
        }
        is(2.U) { // ISZ
          io.mem.req   := true.B
          io.mem.addr  := mar
          io.mem.write := false.B
          when(io.mem.ready) {
            md    := io.mem.rdata + 1.U
            state := sWRITEBACK
          }
        }
        is(3.U) { // DCA
          io.mem.req   := true.B
          io.mem.addr  := mar
          io.mem.write := true.B
          io.mem.wdata := accReg.io.out_data
          when(io.mem.ready) {
            accReg.io.clear := true.B // DCA clears accumulator
            state           := sFETCH
          }
        }
        is(4.U) { // JMS
          io.mem.req   := true.B
          io.mem.addr  := mar
          io.mem.write := true.B
          io.mem.wdata := pc // Write return address (current PC)
          when(io.mem.ready) {
            pc    := mar + 1.U // Jump to subroutine start (mar + 1)
            state := sFETCH
          }
        }
        is(5.U) { // JMP
          pc    := mar
          state := sFETCH
        }
      }
    }
    
    is(sWRITEBACK) {
      val op = inst(11, 9)
      switch(op) {
        is(0.U) { // AND
          accReg.io.load := true.B
          state          := sFETCH
        }
        is(1.U) { // TAD
          accReg.io.load      := true.B
          accReg.io.link_load := true.B
          state               := sFETCH
        }
        is(2.U) { // ISZ
          io.mem.req   := true.B
          io.mem.addr  := mar
          io.mem.write := true.B
          io.mem.wdata := md
          when(io.mem.ready) {
            when(md === 0.U) {
              pc := pc + 1.U // Skip next instruction if zero
            }
            state := sFETCH
          }
        }
      }
    }
  }
}
