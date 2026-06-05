package zoo.execube

import chisel3._
import chisel3.util._
import zoo.common.components._

class ExecubeCore extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 32, dataWidth = 32)
    val hlt = Output(Bool())
    
    // Debug ports
    val pc_debug   = Output(UInt(32.W))
    val regs_debug = Output(Vec(32, UInt(32.W))) // Mock 32 registers
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  val decoder = Module(new ExecubeDecoder)

  // 4 PEs, each having a 2-deep local accumulator stack: index 0 (A), index 1 (B)
  val pe_regs = RegInit(VecInit(Seq.fill(4)(VecInit(Seq.fill(2)(0.U(32.W))))))
  val pc      = RegInit(0.U(32.W))
  val hltReg  = RegInit(false.B)

  io.hlt      := hltReg
  io.pc_debug := pc

  // Map PE accumulator B (regs(1)) to debug ports
  for (i <- 0 until 4) {
    io.regs_debug(i) := pe_regs(i)(1)
  }
  for (i <- 4 until 32) {
    io.regs_debug(i) := 0.U
  }

  val inst        = RegInit(0.U(32.W))
  val target_addr = RegInit(0.U(32.W))

  decoder.io.inst := inst

  val sFETCH :: sDECODE :: sFETCH_ADDR :: sLOAD_PE :: sSTORE_PE :: Nil = Enum(5)
  val state = RegInit(sFETCH)

  io.mem.req   := false.B
  io.mem.addr  := 0.U
  io.mem.write := false.B
  io.mem.wdata := 0.U

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
        val pe_idx = decoder.io.ctrl.rd
        // Accumulator B (index 1) = Accumulator A (index 0) + Accumulator B (index 1)
        pe_regs(pe_idx)(1) := pe_regs(pe_idx)(0) + pe_regs(pe_idx)(1)
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.is_load || decoder.io.ctrl.is_store) {
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
        state       := Mux(decoder.io.ctrl.is_load, sLOAD_PE, sSTORE_PE)
      }
    }

    is(sLOAD_PE) {
      val pe_idx = decoder.io.ctrl.rd
      io.mem.req   := true.B
      // PE loads from target_addr + pe_idx
      io.mem.addr  := target_addr + pe_idx
      io.mem.write := false.B
      when(io.mem.ready) {
        pe_regs(pe_idx)(0) := pe_regs(pe_idx)(1)
        pe_regs(pe_idx)(1) := io.mem.rdata
        state := sFETCH
      }
    }

    is(sSTORE_PE) {
      val pe_idx = decoder.io.ctrl.rd
      io.mem.req   := true.B
      // PE stores to target_addr + pe_idx
      io.mem.addr  := target_addr + pe_idx
      io.mem.write := true.B
      io.mem.wdata := pe_regs(pe_idx)(1)
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
