package zoo.imp16

import chisel3._
import chisel3.util._
import zoo.common.components._

class Imp16Core extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 16, dataWidth = 16)
    val hlt = Output(Bool())
    
    // Debug & PMU
    val pc_debug   = Output(UInt(16.W))
    val regs_debug = Output(Vec(4, UInt(16.W)))
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Registers & Submodules
  val regs = RegInit(VecInit(Seq.fill(4)(0.U(16.W))))
  val decoder = Module(new Imp16Decoder)
  val alu = Module(new ParameterizedALU(width = 16))

  val pc = RegInit(0.U(16.W))
  val inst = RegInit(0.U(16.W))
  val mar = RegInit(0.U(16.W))
  val md = RegInit(0.U(16.W))
  val hltReg = RegInit(false.B)

  io.hlt := hltReg
  io.pc_debug := pc
  io.regs_debug := regs

  // ALU connections
  alu.io.a := regs(decoder.io.rs1_addr(1, 0))
  alu.io.b := regs(decoder.io.rs2_addr(1, 0))
  alu.io.op := decoder.io.ctrl.alu_op

  // Decoder inputs
  decoder.io.inst := inst

  // FSM States
  val sFETCH :: sDECODE :: sFETCH_ADDR :: sEXECUTE :: sWRITEBACK :: Nil = Enum(5)
  val state = RegInit(sFETCH)

  // Opcode decoder helper
  val opcode = inst(15, 12)

  // Default IO assignments
  io.mem.req := false.B
  io.mem.addr := mar
  io.mem.write := false.B
  io.mem.wdata := md

  // FSM Logic
  switch(state) {
    is(sFETCH) {
      io.mem.req := true.B
      io.mem.addr := pc
      io.mem.write := false.B
      when(io.mem.ready) {
        inst := io.mem.rdata
        pc := pc + 1.U
        state := sDECODE
      }
    }

    is(sDECODE) {
      assert(decoder.io.ctrl.legal, "Unimplemented/illegal instruction")
      val is_double = (opcode === 1.U) || (opcode === 2.U) || (opcode === 7.U) || (opcode === 8.U)
      when(decoder.io.ctrl.is_hlt) {
        hltReg := true.B
        state := sFETCH
      }.elsewhen(is_double) {
        mar := pc
        state := sFETCH_ADDR
      }.otherwise {
        state := sEXECUTE
      }
    }

    is(sFETCH_ADDR) {
      io.mem.req := true.B
      io.mem.addr := pc
      io.mem.write := false.B
      when(io.mem.ready) {
        mar := io.mem.rdata
        pc := pc + 1.U
        state := sEXECUTE
      }
    }

    is(sEXECUTE) {
      when(decoder.io.ctrl.mem_read) {
        io.mem.req := true.B
        io.mem.addr := Mux(opcode === 9.U, regs(decoder.io.rs1_addr(1, 0)), mar)
        io.mem.write := false.B
        when(io.mem.ready) {
          md := io.mem.rdata
          state := sWRITEBACK
        }
      }.elsewhen(decoder.io.ctrl.mem_write) {
        io.mem.req := true.B
        io.mem.addr := Mux(opcode === 10.U, regs(decoder.io.rs2_addr(1, 0)), mar)
        io.mem.write := true.B
        io.mem.wdata := regs(decoder.io.rs1_addr(1, 0))
        when(io.mem.ready) {
          state := sFETCH
        }
      }.elsewhen(decoder.io.ctrl.is_jmp) {
        pc := mar
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.is_jnz) {
        val rd_val = regs(decoder.io.rs1_addr(1, 0))
        when(rd_val =/= 0.U) {
          pc := mar
        }
        state := sFETCH
      }.otherwise {
        // Register-Register ALU instruction
        regs(decoder.io.rd_addr(1, 0)) := alu.io.out
        state := sFETCH
      }
    }

    is(sWRITEBACK) {
      when(decoder.io.ctrl.mem_read) {
        regs(decoder.io.rd_addr(1, 0)) := md
      }
      state := sFETCH
    }
  }

  // PMU logic
  val pmu_cycles = RegInit(0.U(32.W))
  val pmu_insts = RegInit(0.U(32.W))
  val pmu_reads = RegInit(0.U(32.W))
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
  io.pmu_insts := pmu_insts
  io.pmu_reads := pmu_reads
  io.pmu_writes := pmu_writes
}
