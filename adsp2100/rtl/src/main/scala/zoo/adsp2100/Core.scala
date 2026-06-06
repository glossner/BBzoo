package zoo.adsp2100

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * ADI ADSP-2100 Core Processor.
 * A 16-bit DSP core with specific registers AX0, AY0, and AR.
 */
class Adsp2100Core extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 16, dataWidth = 16)
    val hlt = Output(Bool())
    
    // Debug ports
    val pc_debug   = Output(UInt(16.W))
    val ax0_debug  = Output(UInt(16.W))
    val ay0_debug  = Output(UInt(16.W))
    val ar_debug   = Output(UInt(16.W))
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Core Registers
  val pc     = RegInit(0.U(16.W))
  val ax0    = RegInit(0.U(16.W))
  val ay0    = RegInit(0.U(16.W))
  val ar     = RegInit(0.U(16.W))
  val hltReg = RegInit(false.B)

  // Sub-modules
  val decoder = Module(new Adsp2100Decoder)

  // Debug outputs
  io.hlt      := hltReg
  io.pc_debug := pc
  io.ax0_debug := ax0
  io.ay0_debug := ay0
  io.ar_debug  := ar

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
      assert(decoder.io.ctrl.legal, "Unimplemented/illegal instruction: 0x%x", decoder.io.inst)
      when(decoder.io.ctrl.is_hlt) {
        hltReg := true.B
        state  := sFETCH
      }.elsewhen(decoder.io.ctrl.is_addar) {
        ar    := ax0 + ay0
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.is_ldax0 || decoder.io.ctrl.is_lday0 || decoder.io.ctrl.is_star) {
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
        when(decoder.io.ctrl.is_star) {
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
        when(decoder.io.ctrl.is_ldax0) {
          ax0 := io.mem.rdata
        }.otherwise {
          ay0 := io.mem.rdata
        }
        state := sFETCH
      }
    }

    is(sSTORE_MEM) {
      io.mem.req   := true.B
      io.mem.addr  := target_addr
      io.mem.write := true.B
      io.mem.wdata := ar
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
