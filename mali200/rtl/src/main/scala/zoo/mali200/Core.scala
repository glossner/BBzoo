package zoo.mali200

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * ARM Mali-200 Core Processor.
 * A 32-bit SIMD mobile GPU execution core.
 */
class Mali200Core extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 16, dataWidth = 32)
    val hlt = Output(Bool())
    
    // Debug ports
    val pc_debug    = Output(UInt(16.W))
    val v0_0_debug  = Output(UInt(32.W))
    val v1_0_debug  = Output(UInt(32.W))
    val v2_0_debug  = Output(UInt(32.W))
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Core Registers
  val pc     = RegInit(0.U(16.W))
  val vregs  = RegInit(VecInit(Seq.fill(4)(VecInit(Seq.fill(4)(0.U(32.W))))))
  val hltReg = RegInit(false.B)
  val vindex = RegInit(0.U(2.W))

  // Sub-modules
  val decoder = Module(new Mali200Decoder)

  // Debug outputs
  io.hlt        := hltReg
  io.pc_debug   := pc
  io.v0_0_debug := vregs(0)(0)
  io.v1_0_debug := vregs(1)(0)
  io.v2_0_debug := vregs(2)(0)

  // Temp Registers
  val inst        = RegInit(0.U(32.W))
  val target_addr = RegInit(0.U(16.W))

  decoder.io.inst := inst

  // FSM States
  val sFETCH :: sDECODE :: sFETCH_ADDR :: sLOAD_VEC :: sSTORE_VEC :: Nil = Enum(5)
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
      }.elsewhen(decoder.io.ctrl.is_vadd) {
        // Parallel element-wise vector add
        for (i <- 0 until 4) {
          vregs(decoder.io.ctrl.dest_v)(i) := vregs(decoder.io.ctrl.src1_v)(i) + vregs(decoder.io.ctrl.src2_v)(i)
        }
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.is_vld || decoder.io.ctrl.is_vst) {
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
        target_addr := io.mem.rdata(15, 0)
        pc          := pc + 1.U
        vindex      := 0.U
        when(decoder.io.ctrl.is_vst) {
          state := sSTORE_VEC
        }.otherwise {
          state := sLOAD_VEC
        }
      }
    }

    is(sLOAD_VEC) {
      io.mem.req   := true.B
      io.mem.addr  := target_addr + vindex
      io.mem.write := false.B
      when(io.mem.ready) {
        vregs(decoder.io.ctrl.dest_v)(vindex) := io.mem.rdata
        vindex := vindex + 1.U
        when(vindex === 3.U) {
          state := sFETCH
        }
      }
    }

    is(sSTORE_VEC) {
      io.mem.req   := true.B
      io.mem.addr  := target_addr + vindex
      io.mem.write := true.B
      io.mem.wdata := vregs(decoder.io.ctrl.dest_v)(vindex) // dest_v contains the register index to store
      when(io.mem.ready) {
        vindex := vindex + 1.U
        when(vindex === 3.U) {
          state := sFETCH
        }
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
