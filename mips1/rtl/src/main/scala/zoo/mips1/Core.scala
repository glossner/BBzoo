package zoo.mips1

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * MIPS I (R2000) Core Processor.
 * A 32-bit RISC core with 32 registers (R0-R31), where R0 is hardwired to 0.
 */
class Mips1Core extends Module {
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
  val decoder = Module(new Mips1Decoder)

  // Registers
  val regs   = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))
  val pc     = RegInit(0.U(32.W))
  val hltReg = RegInit(false.B)

  // Debug outputs
  io.hlt      := hltReg
  io.pc_debug := pc
  for (i <- 0 until 32) {
    io.regs_debug(i) := Mux(i.U === 0.U, 0.U, regs(i))
  }

  // Helper read/write methods
  def readReg(num: UInt): UInt = {
    Mux(num === 0.U, 0.U, regs(num))
  }

  def writeReg(num: UInt, data: UInt): Unit = {
    when(num =/= 0.U) {
      regs(num) := data
    }
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
      }.elsewhen(decoder.io.ctrl.is_addu) {
        val sum = readReg(decoder.io.ctrl.rs) + readReg(decoder.io.ctrl.rt)
        writeReg(decoder.io.ctrl.rd, sum)
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.is_lw || decoder.io.ctrl.is_sw) {
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
        when(decoder.io.ctrl.is_sw) {
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
        writeReg(decoder.io.ctrl.rt, io.mem.rdata)
        state := sFETCH
      }
    }

    is(sSTORE_MEM) {
      io.mem.req   := true.B
      io.mem.addr  := target_addr
      io.mem.write := true.B
      io.mem.wdata := readReg(decoder.io.ctrl.rt)
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
