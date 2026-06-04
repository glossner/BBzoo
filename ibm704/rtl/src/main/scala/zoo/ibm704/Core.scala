package zoo.ibm704

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * IBM 704 Core Processor.
 * A 36-bit computer with accumulator-based datapath and 15-bit address paths.
 */
class Ibm704Core extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 15, dataWidth = 36)
    val hlt = Output(Bool())

    // Debug ports
    val pc_debug   = Output(UInt(15.W))
    val acc_debug  = Output(UInt(36.W))

    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Sub-modules
  val decoder = Module(new Ibm704Decoder)

  // Core Registers
  val pc     = RegInit(0.U(15.W))
  val acc    = RegInit(0.U(36.W))
  val hltReg = RegInit(false.B)
  val inst   = RegInit(0.U(36.W))

  // Debug outputs
  io.hlt       := hltReg
  io.pc_debug  := pc
  io.acc_debug := acc

  // Decoder inputs
  decoder.io.inst := inst
  val addr = decoder.io.addr

  // FSM States
  val sFETCH :: sDECODE :: sMEM_READ :: sMEM_WRITE :: Nil = Enum(4)
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
      }.elsewhen(decoder.io.ctrl.mem_read) {
        state  := sMEM_READ
      }.elsewhen(decoder.io.ctrl.mem_write) {
        state  := sMEM_WRITE
      }.otherwise {
        state  := sFETCH
      }
    }

    is(sMEM_READ) {
      io.mem.req   := true.B
      io.mem.addr  := addr
      io.mem.write := false.B
      when(io.mem.ready) {
        val readData = io.mem.rdata
        when(decoder.io.ctrl.is_ld) {
          acc := readData
        }.elsewhen(decoder.io.ctrl.is_add) {
          acc := acc + readData
        }.elsewhen(decoder.io.ctrl.is_sub) {
          acc := acc - readData
        }
        state := sFETCH
      }
    }

    is(sMEM_WRITE) {
      io.mem.req   := true.B
      io.mem.addr  := addr
      io.mem.write := true.B
      io.mem.wdata := acc
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
