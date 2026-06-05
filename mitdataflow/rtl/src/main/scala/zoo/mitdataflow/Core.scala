package zoo.mitdataflow

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * MIT Tagged-Token Dataflow Core Processor.
 * Simulates token-matching and firing logic.
 */
class MitDataflowCore extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 32, dataWidth = 32)
    val hlt = Output(Bool())
    
    // Debug ports
    val pc_debug          = Output(UInt(32.W))
    val token_valid_debug = Output(Bool())
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Sub-modules
  val decoder = Module(new MitDataflowDecoder)

  // Token Store (32 nodes, 2 slots each)
  val token_valid = RegInit(VecInit(Seq.fill(32)(VecInit(Seq.fill(2)(false.B)))))
  val token_data  = RegInit(VecInit(Seq.fill(32)(VecInit(Seq.fill(2)(0.U(32.W))))))

  // Core Registers
  val pc     = RegInit(0.U(32.W))
  val inst   = RegInit(0.U(32.W))
  val hltReg = RegInit(false.B)

  io.hlt               := hltReg
  io.pc_debug          := pc
  io.token_valid_debug := token_valid(4)(0) // Debug output showing one of the token valids

  // FSM States
  val sFETCH :: sDECODE :: sFETCH_ADDR :: sLOAD_MEM :: sSTORE_MEM :: Nil = Enum(5)
  val state = RegInit(sFETCH)

  // Default IO assignments
  io.mem.req   := false.B
  io.mem.addr  := 0.U
  io.mem.write := false.B
  io.mem.wdata := 0.U

  // Decoder inputs
  decoder.io.inst := inst

  val target_addr = RegInit(0.U(32.W))
  val inst_addr   = RegInit(0.U(32.W))
  val this_node_id = Wire(UInt(8.W))
  this_node_id := inst_addr

  // FSM Logic
  switch(state) {
    is(sFETCH) {
      io.mem.req   := true.B
      io.mem.addr  := pc
      io.mem.write := false.B
      when(io.mem.ready) {
        inst      := io.mem.rdata
        inst_addr := pc
        pc        := pc + 1.U
        state     := sDECODE
      }
    }

    is(sDECODE) {
      when(decoder.io.ctrl.is_hlt) {
        hltReg := true.B
        state  := sFETCH
      }.elsewhen(decoder.io.ctrl.is_add) {
        // ADD instruction (node is this_node_id)
        val n_id = this_node_id
        val target = decoder.io.ctrl.target_node_id
        when(token_valid(n_id)(0) && token_valid(n_id)(1)) {
          token_data(target)(0)  := token_data(n_id)(0) + token_data(n_id)(1)
          token_valid(target)(0) := true.B
          token_valid(n_id)(0)   := false.B
          token_valid(n_id)(1)   := false.B
        }
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.is_load || decoder.io.ctrl.is_store) {
        state := sFETCH_ADDR
      }.otherwise {
        state := sFETCH
      }
    }

    is(sFETCH_ADDR) {
      io.mem.req   := true.B
      io.mem.addr  := pc
      io.mem.write := false.B
      when(io.mem.ready) {
        target_addr := io.mem.rdata
        pc          := pc + 1.U
        when(decoder.io.ctrl.is_store) {
          state := sSTORE_MEM
        }.otherwise {
          state := sLOAD_MEM
        }
      }
    }

    is(sLOAD_MEM) {
      io.mem.req   := true.B
      io.mem.addr  := target_addr
      io.mem.write := false.B
      when(io.mem.ready) {
        val dest_node = decoder.io.ctrl.node_id
        val dest_slot = decoder.io.ctrl.slot(0) // Slot 0 or Slot 1
        token_data(dest_node)(dest_slot)  := io.mem.rdata
        token_valid(dest_node)(dest_slot) := true.B
        state := sFETCH
      }
    }

    is(sSTORE_MEM) {
      val n_id = this_node_id
      val slot = decoder.io.ctrl.slot(0)
      io.mem.req   := true.B
      io.mem.addr  := target_addr
      io.mem.write := true.B
      io.mem.wdata := token_data(n_id)(slot)
      when(io.mem.ready) {
        token_valid(n_id)(slot) := false.B
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
