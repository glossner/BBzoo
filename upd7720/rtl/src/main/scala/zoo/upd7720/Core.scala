package zoo.upd7720

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * NEC uPD7720 Core Processor.
 * A 16-bit DSP core with two accumulators (accA, accB).
 */
class Upd7720Core extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 16, dataWidth = 16)
    val hlt = Output(Bool())
    
    // Debug ports
    val pc_debug    = Output(UInt(16.W))
    val acca_debug  = Output(UInt(16.W))
    val accb_debug  = Output(UInt(16.W))
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Core Registers
  val pc     = RegInit(0.U(16.W))
  val accA   = RegInit(0.U(16.W))
  val accB   = RegInit(0.U(16.W))
  val hltReg = RegInit(false.B)

  // Sub-modules
  val decoder = Module(new Upd7720Decoder)

  // Debug outputs
  io.hlt        := hltReg
  io.pc_debug   := pc
  io.acca_debug := accA
  io.accb_debug := accB

  // Temp Registers
  val inst        = RegInit(0.U(16.W))
  val target_addr = RegInit(0.U(16.W))

  decoder.io.inst := inst

  // FSM States
  val sFETCH :: sDECODE :: sFETCH_ADDR :: sLOAD_MEM :: sSTORE_MEM :: Nil = Enum(5)
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
      }.elsewhen(decoder.io.ctrl.is_add) {
        accA  := accA + accB
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.is_lda || decoder.io.ctrl.is_ldb || decoder.io.ctrl.is_sta) {
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
        when(decoder.io.ctrl.is_sta) {
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
        when(decoder.io.ctrl.is_lda) {
          accA := io.mem.rdata
        }.otherwise {
          accB := io.mem.rdata
        }
        state := sFETCH
      }
    }

    is(sSTORE_MEM) {
      io.mem.req   := true.B
      io.mem.addr  := target_addr
      io.mem.write := true.B
      io.mem.wdata := accA
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
