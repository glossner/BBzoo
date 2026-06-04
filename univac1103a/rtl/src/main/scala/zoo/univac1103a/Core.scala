package zoo.univac1103a

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * UNIVAC 1103A Core Processor.
 * A 36-bit two-address memory-to-memory architecture.
 */
class Univac1103aCore extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 15, dataWidth = 36)
    val hlt = Output(Bool())

    // Debug ports
    val pc_debug   = Output(UInt(15.W))
    val inst_debug = Output(UInt(36.W))

    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Sub-modules
  val decoder = Module(new Univac1103aDecoder)

  // Core Registers
  val pc        = RegInit(0.U(15.W))
  val inst      = RegInit(0.U(36.W))
  val operand_u = RegInit(0.U(36.W))
  val operand_v = RegInit(0.U(36.W))
  val hltReg    = RegInit(false.B)

  // Debug outputs
  io.hlt        := hltReg
  io.pc_debug   := pc
  io.inst_debug := inst

  // Decoder inputs
  decoder.io.inst := inst
  val u_addr = decoder.io.u_addr
  val v_addr = decoder.io.v_addr

  // FSM States
  val sFETCH :: sDECODE :: sREAD_U :: sREAD_V :: sWRITE_V :: Nil = Enum(5)
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
        state := sDECODE
      }
    }

    is(sDECODE) {
      val ctrl = decoder.io.ctrl
      when(ctrl.is_hlt) {
        hltReg := true.B
        state  := sFETCH
      }.elsewhen(ctrl.is_tp || ctrl.is_add || ctrl.is_sub) {
        state  := sREAD_U
      }.otherwise {
        pc    := pc + 1.U
        state := sFETCH
      }
    }

    is(sREAD_U) {
      io.mem.req   := true.B
      io.mem.addr  := u_addr
      io.mem.write := false.B
      when(io.mem.ready) {
        operand_u := io.mem.rdata
        when(decoder.io.ctrl.is_tp) {
          state := sWRITE_V
        }.otherwise {
          state := sREAD_V
        }
      }
    }

    is(sREAD_V) {
      io.mem.req   := true.B
      io.mem.addr  := v_addr
      io.mem.write := false.B
      when(io.mem.ready) {
        operand_v := io.mem.rdata
        state     := sWRITE_V
      }
    }

    is(sWRITE_V) {
      io.mem.req   := true.B
      io.mem.addr  := v_addr
      io.mem.write := true.B
      io.mem.wdata := Mux(decoder.io.ctrl.is_tp, operand_u,
                      Mux(decoder.io.ctrl.is_add, operand_v + operand_u, operand_v - operand_u))
      when(io.mem.ready) {
        pc    := pc + 1.U
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
