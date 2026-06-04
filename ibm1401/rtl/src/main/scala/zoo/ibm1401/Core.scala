package zoo.ibm1401

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * IBM 1401 Core Processor.
 * A 36-bit decimal machine implemented with a Memory-to-Memory datapath.
 */
class Ibm1401Core extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 12, dataWidth = 36)
    val hlt = Output(Bool())

    // Debug ports
    val pc_debug   = Output(UInt(12.W))
    val acc_debug  = Output(UInt(36.W)) // Shows temporary register A for debugging

    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Sub-modules
  val decoder = Module(new Ibm1401Decoder)

  // Core Registers
  val pc     = RegInit(0.U(12.W))
  val hltReg = RegInit(false.B)
  val inst   = RegInit(0.U(36.W))
  val tempA  = RegInit(0.U(36.W))
  val tempB  = RegInit(0.U(36.W))

  // Debug outputs
  io.hlt       := hltReg
  io.pc_debug  := pc
  io.acc_debug := tempA

  // Decoder inputs
  decoder.io.inst := inst
  val a_addr = decoder.io.a_addr
  val b_addr = decoder.io.b_addr

  // FSM States
  val sFETCH :: sDECODE :: sREAD_A :: sREAD_B :: sWRITE_B :: Nil = Enum(5)
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
      }.otherwise {
        state  := sREAD_A
      }
    }

    is(sREAD_A) {
      io.mem.req   := true.B
      io.mem.addr  := a_addr
      io.mem.write := false.B
      when(io.mem.ready) {
        tempA := io.mem.rdata
        when(decoder.io.ctrl.is_mc) {
          state := sWRITE_B
        }.otherwise {
          state := sREAD_B
        }
      }
    }

    is(sREAD_B) {
      io.mem.req   := true.B
      io.mem.addr  := b_addr
      io.mem.write := false.B
      when(io.mem.ready) {
        tempB := io.mem.rdata
        state := sWRITE_B
      }
    }

    is(sWRITE_B) {
      io.mem.req   := true.B
      io.mem.addr  := b_addr
      io.mem.write := true.B
      io.mem.wdata := Mux(decoder.io.ctrl.is_mc, tempA,
                      Mux(decoder.io.ctrl.is_add, tempB + tempA,
                                                  tempB - tempA))
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
