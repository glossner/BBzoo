package zoo.tms320c6k

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

  val sFetch :: sExecute :: sLSU :: sHalt :: Nil = Enum(4)
  val state = RegInit(sFetch)

  // Registers: R0-R15 (32-bit)
  val regs = RegInit(VecInit(Seq.fill(16)(0.U(32.W))))

  // PC and packet buffers
  val pc = RegInit(0.U(32.W))
  val nextPc = RegInit(0.U(32.W))
  val jumpTaken = RegInit(false.B)

  val packetBuffer = Reg(Vec(8, UInt(32.W)))
  val packetSize   = RegInit(0.U(3.W))
  val packetIndex  = RegInit(0.U(3.W))

  // PMU Counters
  val cycles = RegInit(0.U(64.W))
  val insts  = RegInit(0.U(64.W))
  val reads  = RegInit(0.U(64.W))
  val writes = RegInit(0.U(64.W))

  cycles := cycles + 1.U

  // Decoder
  val decoder = Module(new Decoder)
  decoder.io.inst := packetBuffer(packetIndex)

  // Defaults
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
        val fetchedInst = io.mem.rdata
        packetBuffer(packetSize) := fetchedInst
        val newSize = packetSize + 1.U
        packetSize := newSize
        pc := pc + 1.U
        
        // Bit 0 is the p_bit
        when(fetchedInst(0) === 1.U && newSize < 8.U) {
          // Keep fetching parallel packet
          state := sFetch
        }.otherwise {
          // Packet complete, execute it
          packetIndex := 0.U
          jumpTaken   := false.B
          state       := sExecute
        }
      }
    }

    is(sExecute) {
      when(packetIndex === packetSize) {
        // End of packet execution
        when(jumpTaken) {
          pc := nextPc
        }
        packetSize := 0.U
        state      := sFetch
      }.otherwise {
        // Execute instruction at packetIndex
        val op = decoder.io.op
        when(op === 5.U) { // HLT
          insts := insts + 1.U
          state := sHalt
        }.elsewhen(op === 6.U) { // JMP
          nextPc    := decoder.io.target
          jumpTaken := true.B
          packetIndex := packetIndex + 1.U
          insts := insts + 1.U
        }.elsewhen(op === 7.U) { // LD_CU
          regs(decoder.io.rd) := decoder.io.target
          packetIndex := packetIndex + 1.U
          insts := insts + 1.U
        }.elsewhen(op === 3.U) { // ADD
          regs(decoder.io.rd) := regs(decoder.io.rs1) + regs(decoder.io.rs2)
          packetIndex := packetIndex + 1.U
          insts := insts + 1.U
        }.elsewhen(op === 4.U) { // SUB
          regs(decoder.io.rd) := regs(decoder.io.rs1) - regs(decoder.io.rs2)
          packetIndex := packetIndex + 1.U
          insts := insts + 1.U
        }.elsewhen(op === 1.U || op === 2.U) { // LDW or STW
          state := sLSU
        }.otherwise {
          // Unknown instruction, just skip
          packetIndex := packetIndex + 1.U
          insts := insts + 1.U
        }
      }
    }

    is(sLSU) {
      val op = decoder.io.op
      io.mem.req := true.B
      
      when(op === 1.U) { // LDW *rs1, rd
        io.mem.addr := regs(decoder.io.rs1)
        when(io.mem.ready) {
          regs(decoder.io.rd) := io.mem.rdata
          reads := reads + 1.U
          insts := insts + 1.U
          packetIndex := packetIndex + 1.U
          state := sExecute
        }
      }.elsewhen(op === 2.U) { // STW rs1, *rd
        io.mem.addr  := regs(decoder.io.rs1)
        io.mem.write := true.B
        io.mem.wdata := regs(decoder.io.rd)
        when(io.mem.ready) {
          writes := writes + 1.U
          insts := insts + 1.U
          packetIndex := packetIndex + 1.U
          state := sExecute
        }
      }.otherwise {
        // Fallback safety
        packetIndex := packetIndex + 1.U
        state := sExecute
      }
    }
  }
}
