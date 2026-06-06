package zoo.powervr1

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * PowerVR Series 1 Core Processor.
 * A 32-bit GPU execution unit featuring Tile-Based Hidden Surface Removal.
 */
class Powervr1Core extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 16, dataWidth = 32)
    val hlt = Output(Bool())
    
    // Debug ports
    val pc_debug   = Output(UInt(16.W))
    val r0_debug   = Output(UInt(32.W))
    val r1_debug   = Output(UInt(32.W))
    val r2_debug   = Output(UInt(32.W))
    val r3_debug   = Output(UInt(32.W))
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Core Registers
  val pc        = RegInit(0.U(16.W))
  val gprs      = RegInit(VecInit(Seq.fill(4)(0.U(32.W))))
  val hltReg    = RegInit(false.B)
  val depthReg  = RegInit(100.U(32.W)) // Internal Depth Buffer register

  // Sub-modules
  val decoder = Module(new Powervr1Decoder)

  // Debug outputs
  io.hlt      := hltReg
  io.pc_debug := pc
  io.r0_debug := gprs(0)
  io.r1_debug := gprs(1)
  io.r2_debug := gprs(2)
  io.r3_debug := gprs(3)

  // Temp Registers
  val inst        = RegInit(0.U(32.W))
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
      assert(decoder.io.ctrl.legal, "Unimplemented/illegal instruction")
      when(decoder.io.ctrl.is_hlt) {
        hltReg := true.B
        state  := sFETCH
      }.elsewhen(decoder.io.ctrl.is_hsr) {
        val depthVal = gprs(decoder.io.ctrl.depth_reg)
        when(depthVal <= depthReg) {
          gprs(decoder.io.ctrl.dest_reg) := gprs(decoder.io.ctrl.src1_reg) + gprs(decoder.io.ctrl.src2_reg)
          depthReg := depthVal
        }
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.is_ld || decoder.io.ctrl.is_st) {
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
        target_addr := io.mem.rdata(15, 0)
        pc          := pc + 1.U
        when(decoder.io.ctrl.is_st) {
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
        gprs(decoder.io.ctrl.dest_reg) := io.mem.rdata
        state := sFETCH
      }
    }

    is(sSTORE_MEM) {
      io.mem.req   := true.B
      io.mem.addr  := target_addr
      io.mem.write := true.B
      io.mem.wdata := gprs(decoder.io.ctrl.dest_reg)
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
