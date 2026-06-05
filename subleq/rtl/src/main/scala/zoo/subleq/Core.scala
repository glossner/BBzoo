package zoo.subleq

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * SUBLEQ Core Processor.
 * Executing only SUBLEQ A, B, C: Mem[B] = Mem[B] - Mem[A], jump to C if <= 0.
 */
class SubleqCore extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 32, dataWidth = 32)
    val hlt = Output(Bool())
    
    // Debug ports
    val pc_debug = Output(UInt(32.W))
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Core Registers
  val pc     = RegInit(0.U(32.W))
  val hltReg = RegInit(false.B)

  // Instruction word operands
  val a_addr   = RegInit(0.U(32.W))
  val b_addr   = RegInit(0.U(32.W))
  val c_target = RegInit(0.U(32.W))

  // Data values
  val a_val = RegInit(0.U(32.W))
  val b_val = RegInit(0.U(32.W))

  io.hlt      := hltReg
  io.pc_debug := pc

  // FSM States
  val sFETCH_A :: sFETCH_B :: sFETCH_C :: sREAD_A :: sREAD_B :: sWRITE_B :: Nil = Enum(6)
  val state = RegInit(sFETCH_A)

  // Default IO assignments
  io.mem.req   := false.B
  io.mem.addr  := 0.U
  io.mem.write := false.B
  io.mem.wdata := 0.U

  // FSM Logic
  switch(state) {
    is(sFETCH_A) {
      io.mem.req   := true.B
      io.mem.addr  := pc
      io.mem.write := false.B
      when(io.mem.ready) {
        a_addr := io.mem.rdata
        pc     := pc + 1.U
        state  := sFETCH_B
      }
    }

    is(sFETCH_B) {
      io.mem.req   := true.B
      io.mem.addr  := pc
      io.mem.write := false.B
      when(io.mem.ready) {
        b_addr := io.mem.rdata
        pc     := pc + 1.U
        state  := sFETCH_C
      }
    }

    is(sFETCH_C) {
      io.mem.req   := true.B
      io.mem.addr  := pc
      io.mem.write := false.B
      when(io.mem.ready) {
        val target = io.mem.rdata
        c_target := target
        pc       := pc + 1.U
        
        when(target === 0xFFFFFFFFL.U) {
          hltReg := true.B
          state  := sFETCH_A
        }.otherwise {
          state := sREAD_A
        }
      }
    }

    is(sREAD_A) {
      io.mem.req   := true.B
      io.mem.addr  := a_addr
      io.mem.write := false.B
      when(io.mem.ready) {
        a_val := io.mem.rdata
        state := sREAD_B
      }
    }

    is(sREAD_B) {
      io.mem.req   := true.B
      io.mem.addr  := b_addr
      io.mem.write := false.B
      when(io.mem.ready) {
        b_val := io.mem.rdata
        state := sWRITE_B
      }
    }

    is(sWRITE_B) {
      val res = b_val - a_val
      io.mem.req   := true.B
      io.mem.addr  := b_addr
      io.mem.write := true.B
      io.mem.wdata := res
      when(io.mem.ready) {
        when(res.asSInt <= 0.S) {
          pc := c_target
        }
        state := sFETCH_A
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
  when(state === sFETCH_A && io.mem.ready) {
    pmu_insts := pmu_insts + 1.U
  }

  io.pmu_cycles := pmu_cycles
  io.pmu_insts  := pmu_insts
  io.pmu_reads  := pmu_reads
  io.pmu_writes := pmu_writes
}
