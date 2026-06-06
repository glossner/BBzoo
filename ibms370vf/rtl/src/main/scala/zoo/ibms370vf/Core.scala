package zoo.ibms370vf

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
  val sFetch :: sDecode :: sVectorLoad :: sVectorStore :: sVectorArithmetic :: sHalt :: Nil = Enum(6)
  val state = RegInit(sFetch)

  // 8 registers (32-bit GPRs)
  val regs = RegInit(VecInit(Seq.fill(8)(0.U(32.W))))

  // 4 vector registers, each with 4 elements (32-bit)
  val vectorRegs = RegInit(VecInit(Seq.fill(4)(VecInit(Seq.fill(4)(0.U(32.W))))))

  // Vector Count register (VCT)
  val vct = RegInit(4.U(3.W))

  // PC and Control
  val pc = RegInit(0.U(32.W))
  val inst = Reg(UInt(32.W))

  // Vector execution registers
  val memPtr  = Reg(UInt(32.W))
  val vIdx    = Reg(UInt(3.W))
  val vTarget = Reg(UInt(2.W))
  val vSrc1   = Reg(UInt(2.W))
  val vSrc2   = Reg(UInt(2.W))
  val opCode  = Reg(UInt(4.W))

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
      assert(decoder.io.legal, "Unimplemented/illegal instruction")
      val op  = decoder.io.op
      val rd  = decoder.io.rd
      val rs1 = decoder.io.rs1
      val rs2 = decoder.io.rs2
      val target = decoder.io.target

      when(op === 3.U) { // HLT
        insts := insts + 1.U
        state := sHalt
      }.elsewhen(op === 4.U) { // JMP
        pc    := target
        insts := insts + 1.U
        state := sFetch
      }.elsewhen(op === 5.U) { // LD_CU
        regs(rd) := target
        pc    := pc + 1.U
        insts := insts + 1.U
        state := sFetch
      }.elsewhen(op === 6.U) { // VLVC
        vct   := regs(rd)(2, 0)
        pc    := pc + 1.U
        insts := insts + 1.U
        state := sFetch
      }.elsewhen(op === 7.U) { // VLD
        memPtr  := regs(rs1)
        vIdx    := 0.U
        vTarget := rd(1, 0)
        state   := sVectorLoad
      }.elsewhen(op === 8.U) { // VST
        memPtr  := regs(rs1)
        vIdx    := 0.U
        vTarget := rd(1, 0)
        state   := sVectorStore
      }.elsewhen(op === 1.U || op === 2.U) { // VADD / VSUB
        vIdx    := 0.U
        vTarget := rd(1, 0)
        vSrc1   := rs1(1, 0)
        vSrc2   := rs2(1, 0)
        opCode  := op
        state   := sVectorArithmetic
      }.otherwise {
        pc    := pc + 1.U
        insts := insts + 1.U
        state := sFetch
      }
    }

    is(sVectorLoad) {
      io.mem.req  := true.B
      io.mem.addr := memPtr
      when(io.mem.ready) {
        vectorRegs(vTarget)(vIdx(1, 0)) := io.mem.rdata
        reads  := reads + 1.U
        memPtr := memPtr + 1.U
        val nextIdx = vIdx + 1.U
        vIdx := nextIdx
        when(nextIdx === vct) {
          pc    := pc + 1.U
          insts := insts + 1.U
          state := sFetch
        }
      }
    }

    is(sVectorStore) {
      io.mem.req   := true.B
      io.mem.addr  := memPtr
      io.mem.write := true.B
      io.mem.wdata := vectorRegs(vTarget)(vIdx(1, 0))
      when(io.mem.ready) {
        writes := writes + 1.U
        memPtr := memPtr + 1.U
        val nextIdx = vIdx + 1.U
        vIdx := nextIdx
        when(nextIdx === vct) {
          pc    := pc + 1.U
          insts := insts + 1.U
          state := sFetch
        }
      }
    }

    is(sVectorArithmetic) {
      val elem1 = vectorRegs(vSrc1)(vIdx(1, 0))
      val elem2 = vectorRegs(vSrc2)(vIdx(1, 0))
      vectorRegs(vTarget)(vIdx(1, 0)) := Mux(opCode === 1.U, elem1 + elem2, elem1 - elem2)
      
      val nextIdx = vIdx + 1.U
      vIdx := nextIdx
      when(nextIdx === vct) {
        pc    := pc + 1.U
        insts := insts + 1.U
        state := sFetch
      }
    }
  }
}
