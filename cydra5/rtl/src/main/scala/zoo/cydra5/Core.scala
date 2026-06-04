package zoo.cydra5

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
  val sFetch0 :: sFetch1 :: sDecode :: sExecuteLSU :: sHalt :: Nil = Enum(5)
  val state = RegInit(sFetch0)

  // Register File: R0-R7 (32-bit)
  val regs = RegInit(VecInit(Seq.fill(8)(0.U(32.W))))

  // Control registers
  val pc = RegInit(0.U(32.W))
  val tempWord0 = RegInit(0.U(32.W))
  val tempInst  = RegInit(0.U(64.W))

  // PMU Counters
  val cycles = RegInit(0.U(64.W))
  val insts  = RegInit(0.U(64.W))
  val reads  = RegInit(0.U(64.W))
  val writes = RegInit(0.U(64.W))

  cycles := cycles + 1.U

  // Decoder
  val decoder = Module(new Decoder)
  decoder.io.inst := tempInst

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
    is(sFetch0) {
      io.mem.req  := true.B
      io.mem.addr := pc
      when(io.mem.ready) {
        tempWord0 := io.mem.rdata
        pc        := pc + 1.U
        state     := sFetch1
      }
    }

    is(sFetch1) {
      io.mem.req  := true.B
      io.mem.addr := pc
      when(io.mem.ready) {
        tempInst := Cat(tempWord0, io.mem.rdata)
        state    := sDecode
      }
    }

    is(sDecode) {
      // Decode CTRL slot first
      when(decoder.io.ctrl_op === 2.U) { // HLT
        insts := insts + 1.U
        state := sHalt
      }.otherwise {
        // Execute ALU op in parallel
        val aluRes = WireInit(0.U(32.W))
        val aluWrite = WireInit(false.B)
        when(decoder.io.alu_op =/= 0.U) {
          val rs1Val = regs(decoder.io.alu_rs1)
          val rs2Val = regs(decoder.io.alu_rs2)
          aluWrite := true.B
          when(decoder.io.alu_op === 1.U) {
            aluRes := rs1Val + rs2Val
          }.otherwise {
            aluRes := rs1Val - rs2Val
          }
        }

        // Execute LSU op (standalone LD_CU load)
        when(decoder.io.ctrl_op === 3.U) { // LD_CU
          regs(decoder.io.alu_rs1) := Cat(0.U(16.W), decoder.io.target)
          pc    := pc + 1.U
          insts := insts + 1.U
          state := sFetch0
        }.elsewhen(decoder.io.lsu_op =/= 0.U) {
          // Write ALU results if active
          when(aluWrite) {
            regs(decoder.io.alu_rd) := aluRes
          }
          state := sExecuteLSU
        }.otherwise {
          // Write ALU results if active
          when(aluWrite) {
            regs(decoder.io.alu_rd) := aluRes
          }
          // Normal branch/PC advance and Rotate Registers
          val nextPc = WireInit(pc + 1.U)
          when(decoder.io.ctrl_op === 1.U) { // BR_ROT
            nextPc := Cat(0.U(16.W), decoder.io.target)
            // Shift R4-R7: R7 = R6, R6 = R5, R5 = R4, R4 = 0
            regs(7) := regs(6)
            regs(6) := regs(5)
            regs(5) := regs(4)
            regs(4) := 0.U
          }
          pc    := nextPc
          insts := insts + 1.U
          state := sFetch0
        }
      }
    }

    is(sExecuteLSU) {
      io.mem.req  := true.B
      val baseAddr = regs(decoder.io.lsu_base)
      io.mem.addr := baseAddr
      
      when(decoder.io.lsu_op === 2.U) { // ST
        io.mem.write := true.B
        io.mem.wdata := regs(decoder.io.lsu_reg)
        when(io.mem.ready) {
          writes := writes + 1.U
          val nextPc = WireInit(pc + 1.U)
          when(decoder.io.ctrl_op === 1.U) { // BR_ROT
            nextPc := Cat(0.U(16.W), decoder.io.target)
            regs(7) := regs(6)
            regs(6) := regs(5)
            regs(5) := regs(4)
            regs(4) := 0.U
          }
          pc    := nextPc
          insts := insts + 1.U
          state := sFetch0
        }
      }.otherwise { // LD
        when(io.mem.ready) {
          reads := reads + 1.U
          val loadData = io.mem.rdata
          
          val nextPc = WireInit(pc + 1.U)
          when(decoder.io.ctrl_op === 1.U) { // BR_ROT
            nextPc := Cat(0.U(16.W), decoder.io.target)
            // If rotating, R4 gets the load, and R5, R6, R7 shift
            regs(7) := regs(6)
            regs(6) := regs(5)
            regs(5) := regs(4)
            
            // Check if load target is rotating register
            when(decoder.io.lsu_reg === 4.U) {
              regs(4) := loadData
            }.otherwise {
              regs(4) := 0.U
              regs(decoder.io.lsu_reg) := loadData
            }
          }.otherwise {
            regs(decoder.io.lsu_reg) := loadData
          }
          pc    := nextPc
          insts := insts + 1.U
          state := sFetch0
        }
      }
    }
  }
}
