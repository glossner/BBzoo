package zoo.cdc6600

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * CDC 6600 Core Processor.
 */
class Cdc6600Core extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 18, dataWidth = 60)
    val hlt = Output(Bool())
    
    // Debug ports
    val pc_debug    = Output(UInt(18.W))
    val regsA_debug = Output(Vec(8, UInt(18.W)))
    val regsB_debug = Output(Vec(8, UInt(18.W)))
    val regsX_debug = Output(Vec(8, UInt(60.W)))
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Sub-modules
  val decoder = Module(new Cdc6600Decoder)

  // Core Registers
  val pc      = RegInit(0.U(18.W))
  val regsA   = RegInit(VecInit(Seq.fill(8)(0.U(18.W))))
  val regsB   = RegInit(VecInit(Seq.fill(8)(0.U(18.W))))
  val regsX   = RegInit(VecInit(Seq.fill(8)(0.U(60.W))))
  val hltReg  = RegInit(false.B)

  // Debug outputs
  io.hlt         := hltReg
  io.pc_debug    := pc
  io.regsA_debug := regsA
  io.regsB_debug := regsB
  io.regsX_debug := regsX

  // B0 is hardwired to 0
  def readB(num: UInt): UInt = Mux(num === 0.U, 0.U, regsB(num))

  // Decoded instruction and operand registers
  val inst = RegInit(0.U(60.W))
  val current_i = RegInit(0.U(3.W)) // Store active register index for memory states

  decoder.io.inst := inst

  val opcode = decoder.io.ctrl.opcode
  val i      = decoder.io.ctrl.i
  val j      = decoder.io.ctrl.j
  val k      = decoder.io.ctrl.k
  val K      = decoder.io.ctrl.K

  // FSM States
  val sFETCH :: sDECODE :: sMEM_LOAD :: sMEM_STORE :: Nil = Enum(4)
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
      }.elsewhen(opcode === 1.U) { // Ai = Bj + K
        val addr = readB(j) + K
        regsA(i)  := addr
        current_i := i
        when(i >= 1.U && i <= 5.U) {
          state := sMEM_LOAD
        }.elsewhen(i === 6.U || i === 7.U) {
          state := sMEM_STORE
        }.otherwise {
          state := sFETCH
        }
      }.elsewhen(opcode === 2.U) { // Xi = Xj + Xk
        regsX(i) := regsX(j) + regsX(k)
        state    := sFETCH
      }.elsewhen(opcode === 3.U) { // Xi = Xj - Xk
        regsX(i) := regsX(j) - regsX(k)
        state    := sFETCH
      }.otherwise {
        state    := sFETCH
      }
    }

    is(sMEM_LOAD) {
      io.mem.req   := true.B
      io.mem.addr  := regsA(current_i)
      io.mem.write := false.B
      when(io.mem.ready) {
        regsX(current_i) := io.mem.rdata
        state            := sFETCH
      }
    }

    is(sMEM_STORE) {
      io.mem.req   := true.B
      io.mem.addr  := regsA(current_i)
      io.mem.write := true.B
      io.mem.wdata := regsX(current_i)
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
