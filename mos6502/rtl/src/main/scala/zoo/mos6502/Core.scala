package zoo.mos6502

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * MOS 6502 Core Processor.
 */
class Mos6502Core extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 16, dataWidth = 8)
    val hlt = Output(Bool())
    
    // Debug ports
    val pc_debug  = Output(UInt(16.W))
    val acc_debug = Output(UInt(8.W))
    val c_debug   = Output(Bool())
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Sub-modules
  val decoder = Module(new Mos6502Decoder)

  // Core Registers
  val pc     = RegInit(0.U(16.W))
  val regA   = RegInit(0.U(8.W))
  val regC   = RegInit(false.B)
  val hltReg = RegInit(false.B)

  // Debug outputs
  io.hlt       := hltReg
  io.pc_debug  := pc
  io.acc_debug := regA
  io.c_debug   := regC

  // Temp Registers
  val opcode      = RegInit(0.U(8.W))
  val addr_l      = RegInit(0.U(8.W))
  val target_addr = RegInit(0.U(16.W))

  decoder.io.opcode := opcode

  // FSM States
  val sFETCH :: sDECODE :: sFETCH_IMM :: sFETCH_ADDR_L :: sFETCH_ADDR_H :: sLOAD_ABS :: sSTORE_ABS :: Nil = Enum(7)
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
        opcode := io.mem.rdata
        pc     := pc + 1.U
        state  := sDECODE
      }
    }

    is(sDECODE) {
      when(decoder.io.ctrl.is_brk) {
        hltReg := true.B
        state  := sFETCH
      }.elsewhen(decoder.io.ctrl.is_clc) {
        regC  := false.B
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.lda_imm || decoder.io.ctrl.adc_imm) {
        state := sFETCH_IMM
      }.elsewhen(decoder.io.ctrl.lda_abs || decoder.io.ctrl.sta_abs || decoder.io.ctrl.adc_abs) {
        state := sFETCH_ADDR_L
      }.otherwise {
        state := sFETCH
      }
    }

    is(sFETCH_IMM) {
      io.mem.req   := true.B
      io.mem.addr  := pc
      io.mem.write := false.B
      when(io.mem.ready) {
        pc := pc + 1.U
        when(decoder.io.ctrl.lda_imm) {
          regA := io.mem.rdata
        }.elsewhen(decoder.io.ctrl.adc_imm) {
          val sum = regA +& io.mem.rdata +& regC.asUInt
          regA := sum(7, 0)
          regC := sum(8)
        }
        state := sFETCH
      }
    }

    is(sFETCH_ADDR_L) {
      io.mem.req   := true.B
      io.mem.addr  := pc
      io.mem.write := false.B
      when(io.mem.ready) {
        addr_l := io.mem.rdata
        pc     := pc + 1.U
        state  := sFETCH_ADDR_H
      }
    }

    is(sFETCH_ADDR_H) {
      io.mem.req   := true.B
      io.mem.addr  := pc
      io.mem.write := false.B
      when(io.mem.ready) {
        target_addr := Cat(io.mem.rdata, addr_l)
        pc          := pc + 1.U
        when(decoder.io.ctrl.sta_abs) {
          state := sSTORE_ABS
        }.otherwise {
          state := sLOAD_ABS
        }
      }
    }

    is(sLOAD_ABS) {
      io.mem.req   := true.B
      io.mem.addr  := target_addr
      io.mem.write := false.B
      when(io.mem.ready) {
        when(decoder.io.ctrl.lda_abs) {
          regA := io.mem.rdata
        }.elsewhen(decoder.io.ctrl.adc_abs) {
          val sum = regA +& io.mem.rdata +& regC.asUInt
          regA := sum(7, 0)
          regC := sum(8)
        }
        state := sFETCH
      }
    }

    is(sSTORE_ABS) {
      io.mem.req   := true.B
      io.mem.addr  := target_addr
      io.mem.write := true.B
      io.mem.wdata := regA
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
