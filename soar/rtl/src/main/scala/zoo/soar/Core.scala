package zoo.soar

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * SOAR Core Processor.
 * A 32-bit Berkeley RISC style core with tag check traps on ADD.
 */
class SoarCore extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 32, dataWidth = 32)
    val hlt = Output(Bool())
    
    // Debug ports
    val pc_debug   = Output(UInt(32.W))
    val regs_debug = Output(Vec(32, UInt(32.W)))
    val trap_debug = Output(Bool())
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Sub-modules
  val decoder = Module(new SoarDecoder)

  // Registers
  val regs    = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))
  val pc      = RegInit(0.U(32.W))
  val hltReg  = RegInit(false.B)
  val trapReg = RegInit(false.B)

  // Debug outputs
  io.hlt        := hltReg
  io.pc_debug   := pc
  io.trap_debug := trapReg
  for (i <- 0 until 32) {
    io.regs_debug(i) := regs(i)
  }

  // Temp Registers
  val inst        = RegInit(0.U(32.W))
  val target_addr = RegInit(0.U(32.W))

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
        val op1_tag = regs(decoder.io.ctrl.rs1)(0)
        val op2_tag = regs(decoder.io.ctrl.rs2)(0)
        when (op1_tag =/= op2_tag) {
          trapReg := true.B
        }
        regs(decoder.io.ctrl.rd) := regs(decoder.io.ctrl.rs1) + regs(decoder.io.ctrl.rs2)
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
        when(decoder.io.ctrl.is_store) {
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
        // Tag loaded value with low bit 0 (pointer / integer shifted representation)
        regs(decoder.io.ctrl.rd) := Cat(io.mem.rdata(30, 0), 0.U(1.W))
        state := sFETCH
      }
    }

    is(sSTORE_MEM) {
      io.mem.req   := true.B
      io.mem.addr  := target_addr
      io.mem.write := true.B
      // Untag value by shifting right by 1
      io.mem.wdata := regs(decoder.io.ctrl.rd) >> 1
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
