package zoo.ethlilith

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * Ethlilith Core Processor.
 * A 16-bit stack-based core with stack cache registers.
 */
class EthlilithCore extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 16, dataWidth = 16)
    val hlt = Output(Bool())
    
    // Debug ports
    val pc_debug          = Output(UInt(16.W))
    val tos_debug         = Output(UInt(16.W))
    val tos_valid_debug   = Output(Bool())
    val stack_empty_debug = Output(Bool())
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Sub-modules
  val decoder = Module(new EthlilithDecoder)
  val alu     = Module(new ParameterizedALU(width = 16))
  val stack   = Module(new StackMemory(depth = 16, width = 16))

  // Core Registers
  val pc        = RegInit(0.U(16.W))
  val inst      = RegInit(0.U(16.W))
  val tos       = RegInit(0.U(16.W))
  val tos_valid = RegInit(false.B)
  val hltReg    = RegInit(false.B)

  io.hlt               := hltReg
  io.pc_debug          := pc
  io.tos_debug         := tos
  io.tos_valid_debug   := tos_valid
  io.stack_empty_debug := stack.io.empty

  // FSM States
  val sFETCH :: sDECODE :: sFETCH_ADDR :: sLOAD_MEM :: sSTORE_MEM :: Nil = Enum(5)
  val state = RegInit(sFETCH)

  // Default IO assignments
  io.mem.req   := false.B
  io.mem.addr  := 0.U
  io.mem.write := false.B
  io.mem.wdata := 0.U

  // Decoder inputs
  decoder.io.inst := inst

  // ALU inputs
  alu.io.a  := tos
  alu.io.b  := stack.io.data_out
  alu.io.op := AluOp.ADD

  // Stack inputs defaults
  stack.io.push    := false.B
  stack.io.pop     := false.B
  stack.io.data_in := 0.U

  val target_addr = RegInit(0.U(16.W))

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
      when(decoder.io.ctrl.is_hlt) {
        hltReg := true.B
        state  := sFETCH
      }.elsewhen(decoder.io.ctrl.is_add) {
        tos          := alu.io.out
        stack.io.pop := true.B
        state        := sFETCH
      }.elsewhen(decoder.io.ctrl.is_push || decoder.io.ctrl.is_pop) {
        state := sFETCH_ADDR
      }.otherwise {
        state := sFETCH
      }
    }

    is(sFETCH_ADDR) {
      io.mem.req   := true.B
      io.mem.addr  := pc
      io.mem.write := false.B
      when(io.mem.ready) {
        target_addr := io.mem.rdata
        pc          := pc + 1.U
        when(decoder.io.ctrl.is_pop) {
          state := sSTORE_MEM
        }.otherwise {
          state := sLOAD_MEM
        }
      }
    }

    is(sLOAD_MEM) {
      io.mem.req   := true.B
      io.mem.addr  := target_addr
      io.mem.write := false.B
      when(io.mem.ready) {
        when(tos_valid) {
          stack.io.push    := true.B
          stack.io.data_in := tos
        }
        tos       := io.mem.rdata
        tos_valid := true.B
        state     := sFETCH
      }
    }

    is(sSTORE_MEM) {
      io.mem.req   := true.B
      io.mem.addr  := target_addr
      io.mem.write := true.B
      io.mem.wdata := tos
      when(io.mem.ready) {
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

  // PMU Counter Logic
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
