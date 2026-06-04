package zoo.decvax

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * DEC VAX Core Processor.
 * A 32-bit orthogonal CISC architecture supporting 16 GPRs (R0-R15),
 * where R15 is the Program Counter (PC).
 */
class DecvaxCore extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 32, dataWidth = 32)
    val hlt = Output(Bool())
    
    // Debug ports
    val pc_debug   = Output(UInt(32.W))
    val regs_debug = Output(Vec(16, UInt(32.W)))
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Sub-modules
  val decoder = Module(new DecvaxDecoder)
  val alu     = Module(new ParameterizedALU(width = 32))

  // Core Registers
  val regs = RegInit(VecInit(Seq.fill(16)(0.U(32.W))))
  val pc   = RegInit(0.U(32.W))
  val hltReg = RegInit(false.B)

  // Debug outputs
  io.hlt := hltReg
  io.pc_debug := pc
  for (i <- 0 until 15) {
    io.regs_debug(i) := regs(i)
  }
  io.regs_debug(15) := pc

  // Helper read/write methods
  def readReg(num: UInt): UInt = {
    Mux(num === 15.U, pc, regs(num))
  }

  def writeReg(num: UInt, data: UInt): Unit = {
    when(num === 15.U) {
      pc := data
    }.otherwise {
      regs(num) := data
    }
  }

  // Decoded signals and operand registers
  val inst = RegInit(0.U(32.W))
  val src_val = RegInit(0.U(32.W))
  val dst_val = RegInit(0.U(32.W))
  val dst_addr = RegInit(0.U(32.W))

  decoder.io.inst := inst

  val opcode   = decoder.io.ctrl.opcode
  val src_mode = decoder.io.ctrl.src_mode
  val src_reg  = decoder.io.ctrl.src_reg
  val dst_mode = decoder.io.ctrl.dst_mode
  val dst_reg  = decoder.io.ctrl.dst_reg

  // FSM States
  val sFETCH :: sDECODE :: sFETCH_SRC_IMM :: sFETCH_SRC_MEM :: sFETCH_DST :: sFETCH_DST_MEM :: sEXECUTE :: sWRITEBACK :: Nil = Enum(8)
  val state = RegInit(sFETCH)

  // Default IO assignments
  io.mem.req   := false.B
  io.mem.addr  := 0.U
  io.mem.write := false.B
  io.mem.wdata := 0.U

  // ALU connections
  alu.io.a := dst_val
  alu.io.b := src_val
  // MOVL (0xD0): PASS_B, ADDL2 (0xC0): ADD, SUBL2 (0xC2): SUB
  alu.io.op := Mux(opcode === "hC0".U, AluOp.ADD,
               Mux(opcode === "hC2".U, AluOp.SUB, AluOp.PASS_B))

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
      }.otherwise {
        // Source Specifier Fetching
        when(src_mode === 8.U && src_reg === 15.U) { // PC Autoincrement (Immediate)
          state := sFETCH_SRC_IMM
        }.elsewhen(src_mode === 5.U) { // Register Mode
          src_val := readReg(src_reg)
          state   := sFETCH_DST
        }.otherwise { // Memory Modes (Autoincrement 8, Register Deferred 6)
          state   := sFETCH_SRC_MEM
        }
      }
    }

    is(sFETCH_SRC_IMM) {
      io.mem.req   := true.B
      io.mem.addr  := pc
      io.mem.write := false.B
      when(io.mem.ready) {
        src_val := io.mem.rdata
        pc      := pc + 1.U
        state   := sFETCH_DST
      }
    }

    is(sFETCH_SRC_MEM) {
      io.mem.req   := true.B
      io.mem.addr  := readReg(src_reg)
      io.mem.write := false.B
      when(io.mem.ready) {
        src_val := io.mem.rdata
        when(src_mode === 8.U) { // Autoincrement
          writeReg(src_reg, readReg(src_reg) + 1.U)
        }
        state := sFETCH_DST
      }
    }

    is(sFETCH_DST) {
      // Destination Specifier Fetching
      when(dst_mode === 5.U) { // Register Mode
        dst_val := readReg(dst_reg)
        state   := sEXECUTE
      }.otherwise { // Memory Modes
        state   := sFETCH_DST_MEM
      }
    }

    is(sFETCH_DST_MEM) {
      io.mem.req   := true.B
      io.mem.addr  := readReg(dst_reg)
      io.mem.write := false.B
      when(io.mem.ready) {
        dst_val  := io.mem.rdata
        dst_addr := readReg(dst_reg)
        when(dst_mode === 8.U) { // Autoincrement
          writeReg(dst_reg, readReg(dst_reg) + 1.U)
        }
        state := sEXECUTE
      }
    }

    is(sEXECUTE) {
      when(dst_mode === 5.U) {
        writeReg(dst_reg, alu.io.out)
        state := sFETCH
      }.otherwise {
        state := sWRITEBACK
      }
    }

    is(sWRITEBACK) {
      io.mem.req   := true.B
      io.mem.addr  := dst_addr
      io.mem.write := true.B
      io.mem.wdata := alu.io.out
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
