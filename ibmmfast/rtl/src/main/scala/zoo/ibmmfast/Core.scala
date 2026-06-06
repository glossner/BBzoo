package zoo.ibmmfast

import chisel3._
import chisel3.util._

class Core extends Module {
  val io = IO(new Bundle {
    val mem = new Bundle {
      val req   = Output(Bool())
      val addr  = Output(UInt(16.W))
      val write = Output(Bool())
      val wdata = Output(UInt(16.W))
      val rdata = Input(UInt(16.W))
      val ready = Input(Bool())
    }
    val hlt = Output(Bool())
    val pmu_cycles = Output(UInt(64.W))
    val pmu_insts  = Output(UInt(64.W))
    val pmu_reads  = Output(UInt(64.W))
    val pmu_writes = Output(UInt(64.W))
  })

  // FSM States
  val sFetch0 :: sFetch1 :: sDecode :: sExecutePE :: sHalt :: Nil = Enum(5)
  val state = RegInit(sFetch0)

  // Control Unit registers
  val pc = RegInit(0.U(16.W))
  val cu_regs = RegInit(VecInit(Seq.fill(4)(0.U(16.W)))) // R0-R3 (16-bit)
  
  // Processing Elements (4 PEs), each with 8 registers (R0-R7, 16-bit)
  val pe_regs = RegInit(VecInit(Seq.fill(4)(VecInit(Seq.fill(8)(0.U(16.W))))))

  // Temporary registers for instruction assembly and execution
  val tempWord0 = RegInit(0.U(16.W))
  val tempInst  = RegInit(0.U(32.W))
  val peIdx     = RegInit(0.U(3.W))

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
      assert(decoder.io.legal, "Unimplemented/illegal instruction")
      when(decoder.io.is_hlt) {
        insts := insts + 1.U
        state := sHalt
      }.elsewhen(decoder.io.is_jmp) {
        pc    := decoder.io.target_addr
        insts := insts + 1.U
        state := sFetch0
      }.elsewhen(decoder.io.is_ld_cu) {
        cu_regs(decoder.io.cu_rd) := decoder.io.target_addr
        pc    := pc + 1.U
        insts := insts + 1.U
        state := sFetch0
      }.elsewhen(decoder.io.is_vliw) {
        // 1. Execute ALU slot on all PEs
        when(decoder.io.alu_op =/= 0.U) {
          for (i <- 0 until 4) {
            val rs1Val = pe_regs(i)(decoder.io.alu_rs1)
            val rs2Val = pe_regs(i)(decoder.io.alu_rs2) // rs2 index width 2
            val res = WireInit(0.U(16.W))
            when(decoder.io.alu_op === 1.U) {
              res := rs1Val + rs2Val
            }.elsewhen(decoder.io.alu_op === 2.U) {
              res := rs1Val - rs2Val
            }.otherwise {
              res := rs1Val & rs2Val
            }
            pe_regs(i)(decoder.io.alu_rd) := res
          }
        }

        // 2. Execute MAU slot on all PEs
        when(decoder.io.mau_op =/= 0.U) {
          for (i <- 0 until 4) {
            val rs1Val = pe_regs(i)(decoder.io.mau_rs1)
            val rs2Val = pe_regs(i)(decoder.io.mau_rs2) // rs2 index width 2
            val rdVal  = pe_regs(i)(decoder.io.mau_rd)
            val res = WireInit(0.U(16.W))
            val prod = rs1Val * rs2Val
            when(decoder.io.mau_op === 1.U) {
              res := prod(15, 0)
            }.elsewhen(decoder.io.mau_op === 2.U) {
              res := rdVal + prod(15, 0)
            }.otherwise {
              res := rdVal - prod(15, 0)
            }
            pe_regs(i)(decoder.io.mau_rd) := res
          }
        }

        // 3. Check LSU slot
        when(decoder.io.lsu_op =/= 0.U) {
          peIdx := 0.U
          state := sExecutePE
        }.otherwise {
          pc    := pc + 1.U
          insts := insts + 1.U
          state := sFetch0
        }
      }.otherwise {
        state := sHalt
      }
    }

    is(sExecutePE) {
      when(peIdx < 4.U) {
        io.mem.req  := true.B
        val baseAddr = cu_regs(decoder.io.lsu_base(1, 0)) // lsu_base width 5, index R0-R3
        io.mem.addr := baseAddr + peIdx
        
        when(decoder.io.lsu_op === 2.U) { // ST
          io.mem.write := true.B
          io.mem.wdata := pe_regs(peIdx)(decoder.io.lsu_reg)
          when(io.mem.ready) {
            writes := writes + 1.U
            peIdx  := peIdx + 1.U
          }
        }.otherwise { // LD
          when(io.mem.ready) {
            reads := reads + 1.U
            pe_regs(peIdx)(decoder.io.lsu_reg) := io.mem.rdata
            peIdx := peIdx + 1.U
          }
        }
      }.otherwise {
        pc    := pc + 1.U
        insts := insts + 1.U
        state := sFetch0
      }
    }
  }
}
