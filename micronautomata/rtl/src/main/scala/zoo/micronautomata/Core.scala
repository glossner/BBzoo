package zoo.micronautomata

import chisel3._
import chisel3.util._
import zoo.common.components._

class MicronautomataCore extends Module {
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

  val decoder = Module(new MicronautomataDecoder)

  // 2 state arrays representing parallel transition element states
  val state_regs = RegInit(VecInit(Seq.fill(2)(VecInit(Seq.fill(4)(0.U(32.W))))))
  val pc         = RegInit(0.U(32.W))
  val hltReg     = RegInit(false.B)

  io.hlt      := hltReg
  io.pc_debug := pc

  // Map state registers for debug inspection
  for (i <- 0 until 8) {
    io.regs_debug(i) := state_regs(i / 4)(i % 4)
  }
  for (i <- 8 until 32) {
    io.regs_debug(i) := 0.U
  }

  val inst        = RegInit(0.U(32.W))
  val target_addr = RegInit(0.U(32.W))
  val element_idx = RegInit(0.U(2.W))

  decoder.io.inst := inst

  val sFETCH :: sDECODE :: sFETCH_ADDR :: sLOAD_IN :: sSTORE_OUT :: Nil = Enum(5)
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
        val rd  = decoder.io.ctrl.rd
        val rs1 = decoder.io.ctrl.rs1
        val rs2 = decoder.io.ctrl.rs2
        for (i <- 0 until 4) {
          state_regs(rd)(i) := state_regs(rs1)(i) + state_regs(rs2)(i)
        }
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.is_in || decoder.io.ctrl.is_out) {
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
        element_idx := 0.U
        state       := Mux(decoder.io.ctrl.is_in, sLOAD_IN, sSTORE_OUT)
      }
    }

    is(sLOAD_IN) {
      io.mem.req   := true.B
      io.mem.addr  := target_addr + element_idx
      io.mem.write := false.B
      when(io.mem.ready) {
        state_regs(decoder.io.ctrl.rd)(element_idx) := io.mem.rdata
        element_idx := element_idx + 1.U
        when(element_idx === 3.U) {
          state := sFETCH
        }
      }
    }

    is(sSTORE_OUT) {
      io.mem.req   := true.B
      io.mem.addr  := target_addr + element_idx
      io.mem.write := true.B
      io.mem.wdata := state_regs(decoder.io.ctrl.rd)(element_idx)
      when(io.mem.ready) {
        element_idx := element_idx + 1.U
        when(element_idx === 3.U) {
          state := sFETCH
        }
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
