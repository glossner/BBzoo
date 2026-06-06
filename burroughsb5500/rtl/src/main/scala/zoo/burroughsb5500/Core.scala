package zoo.burroughsb5500

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * Burroughs B5500 Core Processor.
 */
class B5500Core extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 15, dataWidth = 48)
    val hlt = Output(Bool())
    
    // Debug ports
    val pc_debug          = Output(UInt(15.W))
    val tos_debug         = Output(UInt(48.W))
    val tos_valid_debug   = Output(Bool())
    val stack_empty_debug = Output(Bool())
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Sub-modules
  val decoder = Module(new B5500Decoder)
  val alu     = Module(new ParameterizedALU(width = 48))
  val stack   = Module(new StackMemory(depth = 16, width = 48))

  // Core Registers
  val pc        = RegInit(0.U(15.W))
  val inst      = RegInit(0.U(48.W))
  val tos       = RegInit(0.U(48.W))
  val tos_valid = RegInit(false.B)
  val hltReg    = RegInit(false.B)

  io.hlt               := hltReg
  io.pc_debug          := pc
  io.tos_debug         := tos
  io.tos_valid_debug   := tos_valid
  io.stack_empty_debug := stack.io.empty

  // FSM States
  val sFETCH :: sDECODE :: sMEM_REQ :: Nil = Enum(3)
  val state = RegInit(sFETCH)

  // Default IO assignments
  io.mem.req   := false.B
  io.mem.addr  := 0.U
  io.mem.write := false.B
  io.mem.wdata := 0.U

  // Decoder inputs
  decoder.io.inst := inst
  val addr = decoder.io.addr

  // ALU inputs
  alu.io.a  := tos
  alu.io.b  := stack.io.data_out
  alu.io.op := decoder.io.ctrl.alu_op

  // Stack inputs defaults
  stack.io.push    := false.B
  stack.io.pop     := false.B
  stack.io.data_in := 0.U

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
      assert(decoder.io.ctrl.legal, "Unimplemented/illegal instruction")
      when(decoder.io.ctrl.is_hlt) {
        hltReg := true.B
        state  := sFETCH
      }.elsewhen(decoder.io.ctrl.stack_push) {
        // PUSH operation: push old tos to stack memory if valid, then read mem
        when(tos_valid) {
          stack.io.push    := true.B
          stack.io.data_in := tos
        }
        state := sMEM_REQ
      }.elsewhen(decoder.io.ctrl.mem_write) { // POP wait, mem_write
        state := sMEM_REQ
      }.elsewhen(decoder.io.ctrl.stack_pop) { // Pop/arithmetic
        // Perform ADD/SUB
        tos       := alu.io.out
        stack.io.pop := true.B
        state     := sFETCH
      }.otherwise {
        state := sFETCH
      }
    }

    is(sMEM_REQ) {
      io.mem.req  := true.B
      io.mem.addr := addr
      
      when(decoder.io.ctrl.mem_read) {
        io.mem.write := false.B
        when(io.mem.ready) {
          tos       := io.mem.rdata
          tos_valid := true.B
          state     := sFETCH
        }
      }.otherwise {
        // POP (store top of stack)
        io.mem.write := true.B
        io.mem.wdata := tos
        when(io.mem.ready) {
          // Pop next element from stack memory into tos
          stack.io.pop := true.B
          when(!stack.io.empty) {
            tos       := stack.io.data_out
            tos_valid := true.B
          }.otherwise {
            tos_valid := false.B
          }
          state := sFETCH
        }
      }
    }
  }

  // PMU Counter Logic (placed at bottom)
  val pmu_cycles = RegInit(0.U(32.W))
  val pmu_insts  = RegInit(0.U(32.W))
  val pmu_reads  = RegInit(0.U(32.W))
  val pmu_writes = RegInit(0.U(32.W))

  pmu_cycles := pmu_cycles + 1.U
  when(io.mem.req && io.mem.ready) {
    when(io.mem.write) {
      pmu_writes := pmu_writes + 1.U
    }.otherwise {
      pmu_reads := pmu_reads + 1.U
    }
  }
  when(state === sFETCH && io.mem.ready) {
    pmu_insts := pmu_insts + 1.U
  }

  io.pmu_cycles := pmu_cycles
  io.pmu_insts  := pmu_insts
  io.pmu_reads  := pmu_reads
  io.pmu_writes := pmu_writes
}
