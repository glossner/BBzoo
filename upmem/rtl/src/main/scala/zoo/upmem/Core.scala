package zoo.upmem

import chisel3._
import chisel3.util._
import zoo.common.components._

class UpmemCore extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 32, dataWidth = 32)
    val hlt = Output(Bool())
    
    // Debug ports
    val pc_debug   = Output(UInt(32.W))
    val regs_debug = Output(Vec(24, UInt(32.W)))
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  val decoder = Module(new UpmemDecoder)

  // Registers (UPMEM DPU has 24 general purpose registers)
  val regs   = RegInit(VecInit(Seq.fill(24)(0.U(32.W))))
  val pc     = RegInit(0.U(32.W))
  val hltReg = RegInit(false.B)

  io.hlt      := hltReg
  io.pc_debug := pc
  for (i <- 0 until 24) {
    io.regs_debug(i) := regs(i)
  }

  val inst = RegInit(0.U(32.W))
  decoder.io.inst := inst

  val sFETCH :: sDECODE :: sMEM_ACCESS :: Nil = Enum(3)
  val state = RegInit(sFETCH)

  io.mem.req   := false.B
  io.mem.addr  := 0.U
  io.mem.write := false.B
  io.mem.wdata := 0.U

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
      }.elsewhen(decoder.io.ctrl.is_add) {
        regs(decoder.io.ctrl.rd) := regs(decoder.io.ctrl.rs1) + regs(decoder.io.ctrl.rs2)
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.is_sub) {
        regs(decoder.io.ctrl.rd) := regs(decoder.io.ctrl.rs1) - regs(decoder.io.ctrl.rs2)
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.is_addi) {
        regs(decoder.io.ctrl.rd) := (regs(decoder.io.ctrl.rs1).asSInt + decoder.io.ctrl.imm16).asUInt
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.is_bne) {
        val rs1_val = regs(decoder.io.ctrl.rd)
        val rs2_val = regs(decoder.io.ctrl.rs1)
        when(rs1_val =/= rs2_val) {
          // Relative displacement is already in words and relative to PC after fetch (PC+1)
          pc := (pc.asSInt + decoder.io.ctrl.imm16).asUInt
        }
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.is_lw || decoder.io.ctrl.is_sw) {
        state := sMEM_ACCESS
      }.otherwise {
        state := sFETCH
      }
    }

    is(sMEM_ACCESS) {
      io.mem.req   := true.B
      io.mem.addr  := (regs(decoder.io.ctrl.rs1).asSInt + decoder.io.ctrl.imm16).asUInt
      io.mem.write := decoder.io.ctrl.is_sw
      io.mem.wdata := regs(decoder.io.ctrl.rd)
      when(io.mem.ready) {
        when(!decoder.io.ctrl.is_sw) {
          regs(decoder.io.ctrl.rd) := io.mem.rdata
        }
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
