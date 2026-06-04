package zoo.babbage

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * Babbage Analytical Engine Core Processor (64-bit word size, representing Mill decimal storage).
 */
class BabbageCore extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 16, dataWidth = 64)
    val hlt = Output(Bool())
    
    // Debug ports
    val pc_debug   = Output(UInt(16.W))
    val mill_debug = Output(UInt(64.W))
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Sub-modules
  val decoder = Module(new BabbageDecoder)
  val alu     = Module(new ParameterizedALU(width = 64))

  // Core Registers
  val pc     = RegInit(0.U(16.W))
  val mill   = RegInit(0.U(64.W))
  val hltReg = RegInit(false.B)
  val inst   = RegInit(0.U(64.W))

  // Debug outputs
  io.hlt        := hltReg
  io.pc_debug   := pc
  io.mill_debug := mill

  // Decoder inputs
  decoder.io.inst := inst
  val addr = decoder.io.addr

  // ALU connections
  alu.io.a  := mill
  alu.io.b  := io.mem.rdata
  alu.io.op := decoder.io.ctrl.alu_op

  // FSM States
  val sFETCH :: sDECODE :: sMEM_READ :: sMEM_WRITE :: Nil = Enum(4)
  val state = RegInit(sFETCH)

  // Default IO assignments
  io.mem.req   := false.B
  io.mem.addr  := 0.U
  io.mem.write := false.B
  io.mem.wdata := 0.U

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
      }.elsewhen(decoder.io.ctrl.mem_read) {
        state  := sMEM_READ
      }.elsewhen(decoder.io.ctrl.mem_write) {
        state  := sMEM_WRITE
      }.otherwise {
        state  := sFETCH
      }
    }

    is(sMEM_READ) {
      io.mem.req   := true.B
      io.mem.addr  := addr
      io.mem.write := false.B
      when(io.mem.ready) {
        mill  := alu.io.out
        state := sFETCH
      }
    }

    is(sMEM_WRITE) {
      io.mem.req   := true.B
      io.mem.addr  := addr
      io.mem.write := true.B
      io.mem.wdata := mill
      when(io.mem.ready) {
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
