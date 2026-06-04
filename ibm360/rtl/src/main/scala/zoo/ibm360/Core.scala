package zoo.ibm360

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * IBM System/360 Core Processor.
 * A synthesizable 32-bit multi-cycle GPR processor with a 24-bit memory interface.
 */
class Ibm360Core extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 24, dataWidth = 32)
    val hlt = Output(Bool())
    
    // Debug outputs for testing/monitoring
    val pc_debug = Output(UInt(24.W))
    val r1_debug = Output(UInt(32.W)) // Value of Register 1
    val r2_debug = Output(UInt(32.W)) // Value of Register 2

    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Sub-modules
  val regFile = Module(new GeneralRegFile(numRegs = 16, width = 32))
  val decoder = Module(new Ibm360Decoder)
  val alu     = Module(new ParameterizedALU(width = 32))

  // Core Registers
  val pc       = RegInit(0.U(24.W))
  val inst     = RegInit(0.U(32.W)) // Latch the instruction (fetched 32 bits)
  val eff_addr = RegInit(0.U(24.W)) // Latch the calculated effective address
  val md       = RegInit(0.U(32.W)) // Memory Data register
  val hltReg   = RegInit(false.B)

  io.hlt      := hltReg
  io.pc_debug := pc

  // FSM States
  val sFETCH :: sDECODE :: sRX_ADDR :: sMEM_REQ :: sEXECUTE :: Nil = Enum(5)
  val state = RegInit(sFETCH)

  // Instruction fields extracted from the latched instruction
  // Note: we left-align the fetched instruction for the decoder
  decoder.io.inst := Cat(inst, 0.U(16.W))

  val r1 = decoder.io.r1
  val r2 = decoder.io.r2
  val x2 = decoder.io.x2
  val b2 = decoder.io.b2
  val d2 = decoder.io.d2

  // Register File Port Routing
  // Port 1 Address:
  // - In sDECODE: read base register B2
  // - Otherwise: read source/destination register R1
  regFile.io.rs1_addr := Mux(state === sDECODE, b2, r1)

  // Port 2 Address:
  // - In sDECODE: read index register X2
  // - Otherwise: read second source register R2 (for RR format)
  regFile.io.rs2_addr := Mux(state === sDECODE, x2, r2)

  // Register write port
  regFile.io.rd_addr := r1
  regFile.io.rd_data := Mux(decoder.io.ctrl.rf_src_mem, md, alu.io.out)
  regFile.io.wen     := (state === sEXECUTE) && decoder.io.ctrl.rf_wen

  // Debug registers exposure
  // We can poke rs1_addr/rs2_addr in a debug state or just hardwire registers 1 & 2 for monitoring
  // For simplicity, we can read from GPR 1 and 2 inside the testbench by exposing their internal state,
  // or we routing them to the output port. Let's routing registers 1 and 2:
  // Since we can't easily read them from outside without ports, we do a peek of the internal Reg in RegFile,
  // but to keep it synthesizable and neat we expose them on the IO port.
  // We can read them by briefly routing them when not in use, or we can just expose the register file outputs.
  // Let's expose the register file outputs for debugging.
  io.r1_debug := regFile.io.rs1_data
  io.r2_debug := regFile.io.rs2_data

  // ALU Port Routing
  // For RR instructions: GPR(R1) and GPR(R2)
  // For RX instructions: GPR(R1) and md (memory data)
  alu.io.a  := regFile.io.rs1_data
  alu.io.b  := Mux(decoder.io.ctrl.is_rx, md, regFile.io.rs2_data)
  alu.io.op := decoder.io.ctrl.alu_op

  // Memory interface routing
  io.mem.req   := false.B
  io.mem.addr  := eff_addr
  io.mem.write := false.B
  io.mem.wdata := regFile.io.rs1_data // Write GPR(R1)

  // FSM Logic
  switch(state) {
    is(sFETCH) {
      io.mem.req   := true.B
      io.mem.addr  := pc
      io.mem.write := false.B
      when(io.mem.ready) {
        inst  := io.mem.rdata
        state := sDECODE
      }
    }

    is(sDECODE) {
      // RegFile addresses are set to B2 and X2. In the next cycle (sRX_ADDR),
      // the values GPR(B2) and GPR(X2) will be available.
      when(decoder.io.ctrl.is_rr) {
        state := sEXECUTE
      }.otherwise {
        state := sRX_ADDR
      }
    }

    is(sRX_ADDR) {
      // Calculate and latch effective address: GPR(B2) + GPR(X2) + D2
      val base_val  = Mux(b2 === 0.U, 0.U, regFile.io.rs1_data)
      val index_val = Mux(x2 === 0.U, 0.U, regFile.io.rs2_data)
      eff_addr := base_val + index_val + d2
      
      state := sMEM_REQ
    }

    is(sMEM_REQ) {
      // Read/Write Memory
      io.mem.req   := true.B
      io.mem.addr  := eff_addr
      io.mem.write := decoder.io.ctrl.mem_write
      
      when(io.mem.ready) {
        when(decoder.io.ctrl.mem_read) {
          md    := io.mem.rdata
          state := sEXECUTE
        }.otherwise {
          // Store complete, advance PC by instruction length (4 bytes)
          pc    := pc + 4.U
          state := sFETCH
        }
      }
    }

    is(sEXECUTE) {
      // Perform writeback if enabled, and advance PC
      val inst_len = decoder.io.ctrl.inst_len
      
      when(decoder.io.ctrl.is_branch) {
        // Unconditional branch mask 15 (BC 15, target)
        when(r1 === 15.U) {
          pc := eff_addr
        }.otherwise {
          pc := pc + inst_len
        }
      }.otherwise {
        pc := pc + inst_len
      }
      
      state := sFETCH
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
