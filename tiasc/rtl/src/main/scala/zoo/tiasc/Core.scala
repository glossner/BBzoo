package zoo.tiasc

import chisel3._
import chisel3.util._

class Core extends Module {
  val io = IO(new Bundle {
    val mem = new Bundle {
      val req   = Output(Bool())
      val addr  = Output(UInt(32.W))
      val write = Output(Bool())
      val wdata = Output(UInt(32.W))
      val rdata = Input(UInt(32.W))
      val ready = Input(Bool())
    }
    val hlt = Output(Bool())
    val pmu_cycles = Output(UInt(64.W))
    val pmu_insts  = Output(UInt(64.W))
    val pmu_reads  = Output(UInt(64.W))
    val pmu_writes = Output(UInt(64.W))
  })

  // FSM States
  val sFetch :: sDecode :: sVectorReadA :: sVectorReadB :: sVectorWrite :: sHalt :: Nil = Enum(6)
  val state = RegInit(sFetch)

  // 8 registers (32-bit scalar GPRs)
  val regs = RegInit(VecInit(Seq.fill(8)(0.U(32.W))))

  // PC and Control
  val pc = RegInit(0.U(32.W))
  val inst = Reg(UInt(32.W))

  // Vector loop tracking registers
  val ptrA = Reg(UInt(32.W))
  val ptrB = Reg(UInt(32.W))
  val ptrC = Reg(UInt(32.W))
  val cnt  = Reg(UInt(32.W))
  val op   = Reg(UInt(4.W))
  val tempA = Reg(UInt(32.W))
  val tempB = Reg(UInt(32.W))

  // PMU Counters
  val cycles = RegInit(0.U(64.W))
  val insts  = RegInit(0.U(64.W))
  val reads  = RegInit(0.U(64.W))
  val writes = RegInit(0.U(64.W))

  cycles := cycles + 1.U

  // Decoder
  val decoder = Module(new Decoder)
  decoder.io.inst := inst

  // Default outputs
  io.mem.req   := false.B
  io.mem.addr  := 0.U
  io.mem.write := false.B
  io.mem.wdata := 0.U
  io.hlt       := (state === sHalt)

  io.pmu_cycles := cycles
  io.pmu_insts  := insts
  io.pmu_reads  := reads
  io.pmu_writes := writes

  switch(state) {
    is(sFetch) {
      io.mem.req  := true.B
      io.mem.addr := pc
      when(io.mem.ready) {
        inst  := io.mem.rdata
        state := sDecode
      }
    }

    is(sDecode) {
      val decOp = decoder.io.op
      val rc    = decoder.io.rc
      val ra    = decoder.io.ra
      val rb    = decoder.io.rb
      val rlen  = decoder.io.rlen
      val target = decoder.io.target

      when(decOp === 3.U) { // HLT
        insts := insts + 1.U
        state := sHalt
      }.elsewhen(decOp === 4.U) { // JMP
        pc    := target
        insts := insts + 1.U
        state := sFetch
      }.elsewhen(decOp === 5.U) { // LD_CU
        regs(rc) := target
        pc    := pc + 1.U
        insts := insts + 1.U
        state := sFetch
      }.elsewhen(decOp === 1.U || decOp === 2.U) { // VADD_ASC / VSUB_ASC
        ptrA := regs(ra)
        ptrB := regs(rb)
        ptrC := regs(rc)
        cnt  := regs(rlen)
        op   := decOp
        state := sVectorReadA
      }.otherwise {
        pc    := pc + 1.U
        insts := insts + 1.U
        state := sFetch
      }
    }

    is(sVectorReadA) {
      io.mem.req  := true.B
      io.mem.addr := ptrA
      when(io.mem.ready) {
        tempA := io.mem.rdata
        reads := reads + 1.U
        state := sVectorReadB
      }
    }

    is(sVectorReadB) {
      io.mem.req  := true.B
      io.mem.addr := ptrB
      when(io.mem.ready) {
        tempB := io.mem.rdata
        reads := reads + 1.U
        state := sVectorWrite
      }
    }

    is(sVectorWrite) {
      io.mem.req   := true.B
      io.mem.addr  := ptrC
      io.mem.write := true.B
      io.mem.wdata := Mux(op === 1.U, tempA + tempB, tempA - tempB)
      when(io.mem.ready) {
        writes := writes + 1.U
        ptrA   := ptrA + 1.U
        ptrB   := ptrB + 1.U
        ptrC   := ptrC + 1.U
        val nextCnt = cnt - 1.U
        cnt    := nextCnt
        when(nextCnt === 0.U) {
          pc    := pc + 1.U
          insts := insts + 1.U
          state := sFetch
        }.otherwise {
          state := sVectorReadA
        }
      }
    }
  }
}
