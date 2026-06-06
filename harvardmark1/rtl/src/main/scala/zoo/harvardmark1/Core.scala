package zoo.harvardmark1

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * Harvard Mark I Core Processor.
 * A 64-bit processor with 72 accumulator/registers.
 */
class HarvardMark1Core extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 16, dataWidth = 64)
    val hlt = Output(Bool())
    
    // Debug and test ports
    val pc_debug   = Output(UInt(16.W))
    val regs_debug = Output(Vec(72, UInt(64.W)))
    
    // Test-write port for pre-loading registers in simulation
    val test_wen   = Input(Bool())
    val test_waddr = Input(UInt(7.W))
    val test_wdata = Input(UInt(64.W))
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Sub-modules
  val decoder = Module(new HarvardMark1Decoder)
  val alu     = Module(new ParameterizedALU(width = 64))

  // Core Registers
  val pc     = RegInit(0.U(16.W))
  val regs   = RegInit(VecInit(Seq.fill(72)(0.U(64.W))))
  val hltReg = RegInit(false.B)
  val inst   = RegInit(0.U(64.W))

  // Debug outputs
  io.hlt        := hltReg
  io.pc_debug   := pc
  io.regs_debug := regs

  // Decoder inputs
  decoder.io.inst := inst

  // Fix index width from 8-bit to 7-bit for 72 registers (log2Ceil(72) = 7)
  val src = decoder.io.src(6, 0)
  val dst = decoder.io.dst(6, 0)

  // ALU connections
  alu.io.a  := regs(dst)
  alu.io.b  := regs(src)
  alu.io.op := decoder.io.ctrl.alu_op

  // FSM States
  val sFETCH :: sDECODE :: Nil = Enum(2)
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
      }.otherwise {
        when(decoder.io.ctrl.write_reg) {
          regs(dst) := alu.io.out
        }
        state := sFETCH
      }
    }
  }

  // Handle register test-write port
  when(io.test_wen) {
    regs(io.test_waddr) := io.test_wdata
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
