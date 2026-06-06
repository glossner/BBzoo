package zoo.illiac4

import chisel3._
import chisel3.util._

class Core extends Module {
  val io = IO(new Bundle {
    val mem = new Bundle {
      val req   = Output(Bool())
      val addr  = Output(UInt(16.W))
      val write = Output(Bool())
      val wdata = Output(UInt(64.W))
      val rdata = Input(UInt(64.W))
      val ready = Input(Bool())
    }
    val hlt = Output(Bool())
    val pmu_cycles = Output(UInt(64.W))
    val pmu_insts  = Output(UInt(64.W))
    val pmu_reads  = Output(UInt(64.W))
    val pmu_writes = Output(UInt(64.W))
  })

  // FSM States
  val sFetch :: sDecode :: sCUReadAddr :: sCUReadData :: sCUWriteData :: sExecutePE :: sHalt :: Nil = Enum(7)
  val state = RegInit(sFetch)

  // Control Unit registers
  val pc = RegInit(0.U(16.W))
  val cu_regs = RegInit(VecInit(Seq.fill(4)(0.U(64.W)))) // R0-R3 (64-bit)
  
  // Processing Elements (4 PEs) local accumulators (A)
  val pe_accs = RegInit(VecInit(Seq.fill(4)(0.U(64.W))))
  val pe_regs_b = RegInit(VecInit(Seq.fill(4)(0.U(64.W)))) // Local B registers for testing ADD_PE_REG

  // Temporary registers for multi-cycle execution
  val tempInst = RegInit(0.U(16.W))
  val tempAddr = RegInit(0.U(16.W))
  val peIdx    = RegInit(0.U(3.W))

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
    is(sFetch) {
      io.mem.req  := true.B
      io.mem.addr := pc
      when(io.mem.ready) {
        tempInst := io.mem.rdata(15, 0)
        state    := sDecode
      }
    }

    is(sDecode) {
      assert(decoder.io.legal, "Unimplemented/illegal instruction")
      when(decoder.io.hlt) {
        insts := insts + 1.U
        state := sHalt
      }.elsewhen(decoder.io.ld_cu || decoder.io.st_cu || decoder.io.jnz_cu || decoder.io.jmp_cu) {
        state := sCUReadAddr
      }.elsewhen(decoder.io.ldi_cu) {
        tempAddr := cu_regs(decoder.io.rs)(15, 0)
        state    := sCUReadData
      }.elsewhen(decoder.io.sti_cu) {
        tempAddr := cu_regs(decoder.io.rd)(15, 0)
        state    := sCUWriteData
      }.elsewhen(decoder.io.ld_pe || decoder.io.st_pe || decoder.io.add_pe) {
        peIdx := 0.U
        state := sExecutePE
      }.elsewhen(decoder.io.route_pe_l) {
        val t = pe_accs(0)
        pe_accs(0) := pe_accs(1)
        pe_accs(1) := pe_accs(2)
        pe_accs(2) := pe_accs(3)
        pe_accs(3) := t
        pc := pc + 1.U
        insts := insts + 1.U
        state := sFetch
      }.elsewhen(decoder.io.route_pe_r) {
        val t = pe_accs(3)
        pe_accs(3) := pe_accs(2)
        pe_accs(2) := pe_accs(1)
        pe_accs(1) := pe_accs(0)
        pe_accs(0) := t
        pc := pc + 1.U
        insts := insts + 1.U
        state := sFetch
      }.elsewhen(decoder.io.add_pe_reg) {
        for (i <- 0 until 4) {
          pe_accs(i) := pe_accs(i) + pe_regs_b(i)
        }
        pc := pc + 1.U
        insts := insts + 1.U
        state := sFetch
      }.otherwise {
        state := sHalt
      }
    }

    is(sCUReadAddr) {
      io.mem.req  := true.B
      io.mem.addr := pc + 1.U
      when(io.mem.ready) {
        tempAddr := io.mem.rdata(15, 0)
        when(decoder.io.jmp_cu) {
          pc    := io.mem.rdata(15, 0)
          insts := insts + 1.U
          state := sFetch
        }.elsewhen(decoder.io.jnz_cu) {
          insts := insts + 1.U
          when(cu_regs(decoder.io.rd) =/= 0.U) {
            pc := io.mem.rdata(15, 0)
          }.otherwise {
            pc := pc + 2.U
          }
          state := sFetch
        }.elsewhen(decoder.io.ld_cu) {
          cu_regs(decoder.io.rd) := io.mem.rdata(15, 0)
          pc := pc + 2.U
          insts := insts + 1.U
          state := sFetch
        }.elsewhen(decoder.io.st_cu) {
          state := sCUWriteData
        }
      }
    }

    is(sCUReadData) {
      io.mem.req  := true.B
      io.mem.addr := tempAddr
      when(io.mem.ready) {
        cu_regs(decoder.io.rd) := io.mem.rdata
        reads := reads + 1.U
        insts := insts + 1.U
        when(decoder.io.ldi_cu) {
          pc := pc + 1.U
        }
        state := sFetch
      }
    }

    is(sCUWriteData) {
      io.mem.req   := true.B
      io.mem.addr  := tempAddr
      io.mem.write := true.B
      io.mem.wdata := cu_regs(decoder.io.rs)
      when(io.mem.ready) {
        writes := writes + 1.U
        insts  := insts + 1.U
        when(decoder.io.sti_cu) {
          pc := pc + 1.U
        }.otherwise {
          pc := pc + 2.U
        }
        state := sFetch
      }
    }

    is(sExecutePE) {
      when(peIdx < 4.U) {
        io.mem.req  := true.B
        io.mem.addr := cu_regs(1)(15, 0) + peIdx
        
        when(decoder.io.st_pe) {
          io.mem.write := true.B
          io.mem.wdata := pe_accs(peIdx)
          when(io.mem.ready) {
            writes := writes + 1.U
            peIdx  := peIdx + 1.U
          }
        }.otherwise {
          when(io.mem.ready) {
            reads := reads + 1.U
            when(decoder.io.ld_pe) {
              pe_accs(peIdx) := io.mem.rdata
            }.otherwise {
              pe_accs(peIdx) := pe_accs(peIdx) + io.mem.rdata
            }
            peIdx := peIdx + 1.U
          }
        }
      }.otherwise {
        pc    := pc + 1.U
        insts := insts + 1.U
        state := sFetch
      }
    }
  }
}
