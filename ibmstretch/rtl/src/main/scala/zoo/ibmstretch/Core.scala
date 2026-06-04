package zoo.ibmstretch

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * IBM Stretch (IBM 7030) Core Processor.
 * A 64-bit CPU with 16 index registers and accumulator-based datapath.
 */
class IbmstretchCore extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 20, dataWidth = 64)
    val hlt = Output(Bool())

    // Debug ports
    val pc_debug   = Output(UInt(20.W))
    val acc_debug  = Output(UInt(64.W))

    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Sub-modules
  val decoder = Module(new IbmstretchDecoder)

  // Core Registers
  val pc     = RegInit(0.U(20.W))
  val acc    = RegInit(0.U(64.W))
  val hltReg = RegInit(false.B)
  val inst   = RegInit(0.U(64.W))

  // 16 Index Registers (20-bit wide)
  val xregs  = RegInit(VecInit(Seq.fill(16)(0.U(20.W))))

  // Registered effective address for memory operations
  val effAddrReg = RegInit(0.U(20.W))

  // Debug outputs
  io.hlt       := hltReg
  io.pc_debug  := pc
  io.acc_debug := acc

  // Decoder inputs
  decoder.io.inst := inst
  val index_reg = decoder.io.index_reg
  val data_addr = decoder.io.addr

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
        state := sDECODE
      }
    }

    is(sDECODE) {
      val ctrl = decoder.io.ctrl
      effAddrReg := data_addr + xregs(index_reg)

      when(ctrl.is_hlt) {
        hltReg := true.B
        state  := sFETCH
      }.elsewhen(ctrl.is_ldx) {
        xregs(index_reg) := data_addr
        pc               := pc + 1.U
        state            := sFETCH
      }.elsewhen(ctrl.is_addx) {
        xregs(index_reg) := xregs(index_reg) + data_addr
        pc               := pc + 1.U
        state            := sFETCH
      }.elsewhen(ctrl.mem_read) {
        state  := sMEM_READ
      }.elsewhen(ctrl.mem_write) {
        state  := sMEM_WRITE
      }.otherwise {
        pc    := pc + 1.U
        state := sFETCH
      }
    }

    is(sMEM_READ) {
      io.mem.req   := true.B
      io.mem.addr  := effAddrReg
      io.mem.write := false.B
      when(io.mem.ready) {
        when(decoder.io.ctrl.is_ld) {
          acc := io.mem.rdata
        }.elsewhen(decoder.io.ctrl.is_add) {
          acc := acc + io.mem.rdata
        }
        pc    := pc + 1.U
        state := sFETCH
      }
    }

    is(sMEM_WRITE) {
      io.mem.req   := true.B
      io.mem.addr  := effAddrReg
      io.mem.write := true.B
      io.mem.wdata := acc
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
