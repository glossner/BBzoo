package zoo.ibm801

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * IBM 801 Core Processor.
 * A 32-bit RISC core with 32 GPRs (R0-R31).
 */
class Ibm801Core extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 32, dataWidth = 32)
    val hlt = Output(Bool())
    
    // Debug ports
    val pc_debug   = Output(UInt(32.W))
    val regs_debug = Output(Vec(32, UInt(32.W)))
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Sub-modules
  val decoder = Module(new Ibm801Decoder)

  // Registers
  val regs   = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))
  val pc     = RegInit(0.U(32.W))
  val hltReg = RegInit(false.B)

  // Debug outputs
  io.hlt      := hltReg
  io.pc_debug := pc
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
      }.elsewhen(decoder.io.ctrl.opcode === 0x01.U) { // ADD rd, rs1, rs2
        regs(decoder.io.ctrl.rd) := regs(decoder.io.ctrl.rs1) + regs(decoder.io.ctrl.rs2)
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.opcode === 0x02.U || decoder.io.ctrl.opcode === 0x03.U) { // L or ST
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
        when(decoder.io.ctrl.opcode === 0x03.U) {
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
        regs(decoder.io.ctrl.rs1) := io.mem.rdata
        state := sFETCH
      }
    }

    is(sSTORE_MEM) {
      io.mem.req   := true.B
      io.mem.addr  := target_addr
      io.mem.write := true.B
      io.mem.wdata := regs(decoder.io.ctrl.rs1)
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
