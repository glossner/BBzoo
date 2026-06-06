package zoo.tta

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * TTA Core Processor.
 * A transport-triggered architecture with MOVE instructions.
 */
class TtaCore extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 32, dataWidth = 32)
    val hlt = Output(Bool())
    
    // Debug ports
    val pc_debug   = Output(UInt(32.W))
    val regs_debug = Output(Vec(8, UInt(32.W)))
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Sub-modules
  val decoder = Module(new TtaDecoder)

  // Sockets (Registers)
  val regs      = RegInit(VecInit(Seq.fill(8)(0.U(32.W))))
  val add_in1   = RegInit(0.U(32.W))
  val add_in2   = RegInit(0.U(32.W))
  val add_out   = RegInit(0.U(32.W))
  val lsu_addr  = RegInit(0.U(32.W))
  val lsu_rdata = RegInit(0.U(32.W))
  val lsu_wdata = RegInit(0.U(32.W))

  val pc     = RegInit(0.U(32.W))
  val hltReg = RegInit(false.B)

  // Debug outputs
  io.hlt      := hltReg
  io.pc_debug := pc
  for (i <- 0 until 8) {
    io.regs_debug(i) := regs(i)
  }

  // Temp Registers
  val inst    = RegInit(0.U(32.W))
  val src_val = RegInit(0.U(32.W))

  decoder.io.inst := inst

  // FSM States
  val sFETCH :: sDECODE :: sFETCH_IMM :: sEXECUTE :: sLOAD_MEM :: sSTORE_MEM :: Nil = Enum(6)
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
      }.elsewhen(decoder.io.ctrl.is_move_imm) {
        state := sFETCH_IMM
      }.elsewhen(decoder.io.ctrl.is_move) {
        // Resolve source value
        val src = decoder.io.ctrl.src
        val resolved = Wire(UInt(32.W))
        when(src < 8.U) {
          resolved := regs(src)
        }.elsewhen(src === 8.U) {
          resolved := add_in1
        }.elsewhen(src === 9.U) {
          resolved := add_in2
        }.elsewhen(src === 10.U) {
          resolved := add_out
        }.elsewhen(src === 11.U) {
          resolved := lsu_addr
        }.elsewhen(src === 12.U) {
          resolved := lsu_rdata
        }.elsewhen(src === 13.U) {
          resolved := lsu_wdata
        }.otherwise {
          resolved := 0.U
        }
        src_val := resolved
        state   := sEXECUTE
      }.otherwise {
        state := sFETCH
      }
    }

    is(sFETCH_IMM) {
      io.mem.req   := true.B
      io.mem.addr  := pc
      io.mem.write := false.B
      when(io.mem.ready) {
        src_val := io.mem.rdata
        pc      := pc + 1.U
        state   := sEXECUTE
      }
    }

    is(sEXECUTE) {
      val dest = decoder.io.ctrl.dest
      val next_state = WireInit(sFETCH)
      
      when(dest < 8.U) {
        regs(dest) := src_val
      }.elsewhen(dest === 8.U) {
        add_in1 := src_val
      }.elsewhen(dest === 9.U) {
        add_in2 := src_val
        add_out := add_in1 + src_val
      }.elsewhen(dest === 11.U) {
        lsu_addr   := src_val
        next_state := sLOAD_MEM
      }.elsewhen(dest === 13.U) {
        lsu_wdata  := src_val
        next_state := sSTORE_MEM
      }
      
      state := next_state
    }

    is(sLOAD_MEM) {
      io.mem.req   := true.B
      io.mem.addr  := lsu_addr
      io.mem.write := false.B
      when(io.mem.ready) {
        lsu_rdata := io.mem.rdata
        state     := sFETCH
      }
    }

    is(sSTORE_MEM) {
      io.mem.req   := true.B
      io.mem.addr  := lsu_addr
      io.mem.write := true.B
      io.mem.wdata := lsu_wdata
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
