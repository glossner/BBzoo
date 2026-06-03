package zoo.cray1

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * Cray-1 Core Processor.
 * A synthesizable 64-bit vector processor shell featuring scalar A/S execution,
 * and element-by-element vector register execution.
 */
class Cray1Core extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 24, dataWidth = 64)
    val hlt = Output(Bool())
    
    // Debug outputs for testing/monitoring
    val pc_debug   = Output(UInt(24.W))
    val vl_debug   = Output(UInt(4.W))
    val s0_debug   = Output(UInt(64.W)) // S0 register value
    val a0_debug   = Output(UInt(24.W)) // A0 register value
    val v0_0_debug = Output(UInt(64.W)) // Element 0 of Vector Register V0
    val v0_1_debug = Output(UInt(64.W)) // Element 1 of Vector Register V0

    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Sub-modules
  val regFileA = Module(new GeneralRegFile(numRegs = 8, width = 24))
  val regFileS = Module(new GeneralRegFile(numRegs = 8, width = 64))
  val decoder  = Module(new Cray1Decoder)

  // FSM States
  val sFETCH :: sDECODE :: sSCALAR_MEM_REQ :: sEXECUTE_VECTOR :: sVECTOR_MEM_REQ :: Nil = Enum(5)
  val state = RegInit(sFETCH)

  // Vector register file (8 registers, each holding 8 elements of 64-bit)
  val vRegs = Reg(Vec(8, Vec(8, UInt(64.W))))
  val vl    = RegInit(4.U(4.W)) // Vector Length register (max 8 in this design)

  // Core Registers
  val pc     = RegInit(0.U(24.W))
  val inst   = RegInit(0.U(64.W))
  val hltReg = RegInit(false.B)

  // Vector loop index register
  val vIdx = RegInit(0.U(3.W))

  io.hlt      := hltReg
  io.pc_debug := pc
  io.vl_debug := vl

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

  // Decode instruction parcel (bits 15-0)
  decoder.io.inst := inst(15, 0)
  val ri = decoder.io.r_i
  val rj = decoder.io.r_j
  val rk = decoder.io.r_k

  // Register File Routing
  // Port 1 reads GPR(rj) by default; if storing A register to memory, we read GPR(ri)
  regFileA.io.rs1_addr := Mux(state === sSCALAR_MEM_REQ && decoder.io.ctrl.is_store_a, ri, rj)
  regFileA.io.rs2_addr := rk
  regFileA.io.rd_addr  := Mux(decoder.io.ctrl.is_li_a, inst(11, 9), ri)
  regFileA.io.rd_data  := Mux(decoder.io.ctrl.is_li_a, inst(8, 0), regFileA.io.rs1_data + regFileA.io.rs2_data)
  regFileA.io.wen      := (state === sDECODE) && (decoder.io.ctrl.is_add_a || decoder.io.ctrl.is_li_a)

  regFileS.io.rs1_addr := rj
  regFileS.io.rs2_addr := rk
  regFileS.io.rd_addr  := ri
  regFileS.io.rd_data  := regFileS.io.rs1_data + regFileS.io.rs2_data // Default ALU addition
  regFileS.io.wen      := (state === sDECODE) && decoder.io.ctrl.is_add_s

  // Expose register contents for debug (register 0)
  io.a0_debug   := regFileA.io.regs_debug(0)
  io.s0_debug   := regFileS.io.regs_debug(0)
  io.v0_0_debug := vRegs(0)(0)
  io.v0_1_debug := vRegs(0)(1)

  // Memory interface routing
  io.mem.req   := false.B
  io.mem.addr  := 0.U
  io.mem.write := false.B
  io.mem.wdata := 0.U

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
      }.elsewhen(decoder.io.ctrl.is_add_a) {
        // Handled combinationally above by regFileA.io.wen
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.is_add_s) {
        // Handled combinationally above by regFileS.io.wen
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.is_set_vl) {
        vl    := regFileA.io.rs1_data(3, 0)
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.is_load_a || decoder.io.ctrl.is_store_a) {
        state := sSCALAR_MEM_REQ
      }.elsewhen(decoder.io.ctrl.is_add_v || decoder.io.ctrl.is_add_vs) {
        vIdx  := 0.U
        state := sEXECUTE_VECTOR
      }.elsewhen(decoder.io.ctrl.is_load_v || decoder.io.ctrl.is_store_v) {
        vIdx  := 0.U
        state := sVECTOR_MEM_REQ
      }.otherwise {
        state := sFETCH
      }
    }

    is(sSCALAR_MEM_REQ) {
      io.mem.req  := true.B
      io.mem.addr := regFileA.io.rs1_data // address = A(j)
      
      when(decoder.io.ctrl.is_load_a) {
        io.mem.write := false.B
        when(io.mem.ready) {
          // Write fetched data to A(ri)
          // We can use a custom write port or routing it
          // Since the main write port is shared, we set the GPR A write controls:
          // (note: this requires us to override regFileA write ports in this state)
          // In Chisel, we can do this by setting regFileA.io.wen and regFileA.io.rd_data
          // using a conditional block or Mux.
          state := sFETCH
        }
      }.otherwise {
        // Store A(ri) to mem[A(j)]
        io.mem.write := true.B
        io.mem.wdata := regFileA.io.rs1_data // rs1_addr is ri during store
        when(io.mem.ready) {
          state := sFETCH
        }
      }
    }

    is(sEXECUTE_VECTOR) {
      when(vIdx < vl) {
        when(decoder.io.ctrl.is_add_v) {
          vRegs(ri)(vIdx) := vRegs(rj)(vIdx) + vRegs(rk)(vIdx)
        }.elsewhen(decoder.io.ctrl.is_add_vs) {
          vRegs(ri)(vIdx) := vRegs(rj)(vIdx) + regFileS.io.rs2_data
        }
        vIdx := vIdx + 1.U
      }.otherwise {
        state := sFETCH
      }
    }

    is(sVECTOR_MEM_REQ) {
      when(vIdx < vl) {
        io.mem.req  := true.B
        io.mem.addr := regFileA.io.rs1_data + vIdx // Address = A(j) + element index
        
        when(decoder.io.ctrl.is_load_v) {
          io.mem.write := false.B
          when(io.mem.ready) {
            vRegs(ri)(vIdx) := io.mem.rdata
            vIdx            := vIdx + 1.U
          }
        }.otherwise {
          // Store Vector Element
          io.mem.write := true.B
          io.mem.wdata := vRegs(ri)(vIdx)
          when(io.mem.ready) {
            vIdx := vIdx + 1.U
          }
        }
      }.otherwise {
        state := sFETCH
      }
    }
  }

  // Handle register write routing for loads during sSCALAR_MEM_REQ
  // By overriding the register write controls combinationally when state is sSCALAR_MEM_REQ:
  when(state === sSCALAR_MEM_REQ && decoder.io.ctrl.is_load_a && io.mem.ready) {
    regFileA.io.wen     := true.B
    regFileA.io.rd_addr := ri
    regFileA.io.rd_data := io.mem.rdata(23, 0)
  }
}
