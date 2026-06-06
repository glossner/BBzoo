package zoo.zusez1

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * Zuse Z1 Core Processor.
 * A 22-bit floating-point-like scalar machine with R1 and R2 registers.
 */
class ZuseZ1Core extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 16, dataWidth = 22)
    val hlt = Output(Bool())
    
    // Debug ports
    val pc_debug = Output(UInt(16.W))
    val r1_debug = Output(UInt(22.W))
    val r2_debug = Output(UInt(22.W))
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Sub-modules
  val decoder = Module(new ZuseZ1Decoder)
  val alu     = Module(new ParameterizedALU(width = 22))

  // Core Registers
  val pc     = RegInit(0.U(16.W))
  val r1     = RegInit(0.U(22.W))
  val r2     = RegInit(0.U(22.W))
  val hltReg = RegInit(false.B)
  val inst   = RegInit(0.U(22.W))

  // Debug outputs
  io.hlt      := hltReg
  io.pc_debug := pc
  io.r1_debug := r1
  io.r2_debug := r2

  // Decoder inputs
  decoder.io.inst := inst
  val addr = decoder.io.addr

  // ALU connections
  alu.io.a  := r1
  alu.io.b  := r2
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
      assert(decoder.io.ctrl.legal, "Unimplemented/illegal instruction")
      when(decoder.io.ctrl.is_hlt) {
        hltReg := true.B
        state  := sFETCH
      }.elsewhen(decoder.io.ctrl.mov_r1_r2) {
        r2    := r1
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.mem_read) {
        state := sMEM_READ
      }.elsewhen(decoder.io.ctrl.mem_write) {
        state := sMEM_WRITE
      }.otherwise {
        // ALU operation (ADD/SUB)
        r1    := alu.io.out
        state := sFETCH
      }
    }

    is(sMEM_READ) {
      io.mem.req   := true.B
      io.mem.addr  := addr
      io.mem.write := false.B
      when(io.mem.ready) {
        r1    := io.mem.rdata
        state := sFETCH
      }
    }

    is(sMEM_WRITE) {
      io.mem.req   := true.B
      io.mem.addr  := addr
      io.mem.write := true.B
      io.mem.wdata := r1
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
