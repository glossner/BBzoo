package zoo.crusoe

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

  val sFetch :: sExecute :: sExecuteLSU :: sHalt :: Nil = Enum(4)
  val state = RegInit(sFetch)

  // Registers: R0-R15 (32-bit)
  val regs = RegInit(VecInit(Seq.fill(16)(0.U(32.W))))

  // PC and packet buffers (VLIW scheduled slots)
  val pc = RegInit(0.U(32.W))

  val alu_active = RegInit(false.B)
  val alu_op     = RegInit(0.U(4.W))
  val alu_rd     = RegInit(0.U(4.W))
  val alu_rs     = RegInit(0.U(4.W))

  val lsu_active = RegInit(false.B)
  val lsu_op     = RegInit(0.U(4.W))
  val lsu_rd     = RegInit(0.U(4.W))
  val lsu_rs     = RegInit(0.U(4.W))

  val ctrl_active = RegInit(false.B)
  val ctrl_op     = RegInit(0.U(4.W))
  val ctrl_rd     = RegInit(0.U(4.W))
  val ctrl_target = RegInit(0.U(16.W))

  // PMU Counters
  val cycles = RegInit(0.U(64.W))
  val insts  = RegInit(0.U(64.W))
  val reads  = RegInit(0.U(64.W))
  val writes = RegInit(0.U(64.W))

  cycles := cycles + 1.U

  // Decoder for instruction fetched at PC
  val decoder = Module(new Decoder)
  decoder.io.inst := io.mem.rdata

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

  // Combinational Dependency & Scheduling Logic
  val op = decoder.io.op
  val rd = decoder.io.rd
  val rs = decoder.io.rs
  val target = decoder.io.target

  val can_schedule = Wire(Bool())
  can_schedule := false.B

  val has_raw = Wire(Bool())
  val has_war = Wire(Bool())
  val has_waw = Wire(Bool())
  has_raw := false.B
  has_war := false.B
  has_waw := false.B

  // Check dependencies against ALU slot if active
  when(alu_active) {
    // RAW: if current reads alu_rd
    when(op === 3.U || op === 4.U) { // ADD/SUB reads rd, rs
      when(rd === alu_rd || rs === alu_rd) { has_raw := true.B }
    }.elsewhen(op === 1.U) { // Load reads rs
      when(rs === alu_rd) { has_raw := true.B }
    }.elsewhen(op === 2.U) { // Store reads rd, rs
      when(rd === alu_rd || rs === alu_rd) { has_raw := true.B }
    }

    // WAR: if current writes alu_rd or alu_rs
    when(op === 3.U || op === 4.U || op === 1.U || op === 5.U) { // write rd
      when(rd === alu_rd || rd === alu_rs) { has_war := true.B }
    }

    // WAW: if current writes alu_rd
    when(op === 3.U || op === 4.U || op === 1.U || op === 5.U) { // write rd
      when(rd === alu_rd) { has_waw := true.B }
    }
  }

  // Check dependencies against LSU slot if active
  when(lsu_active) {
    // If LSU is Load (writes lsu_rd, reads lsu_rs):
    when(lsu_op === 1.U) {
      when(op === 3.U || op === 4.U) {
        when(rd === lsu_rd || rs === lsu_rd) { has_raw := true.B }
        when(rd === lsu_rs) { has_war := true.B }
        when(rd === lsu_rd) { has_waw := true.B }
      }.elsewhen(op === 1.U) {
        when(rs === lsu_rd) { has_raw := true.B }
        when(rd === lsu_rs) { has_war := true.B }
        when(rd === lsu_rd) { has_waw := true.B }
      }.elsewhen(op === 2.U) {
        when(rd === lsu_rd || rs === lsu_rd) { has_raw := true.B }
      }
    }
    // If LSU is Store (reads lsu_rd, lsu_rs):
    when(lsu_op === 2.U) {
      when(op === 3.U || op === 4.U || op === 1.U || op === 5.U) { // writes rd
        when(rd === lsu_rd || rd === lsu_rs) { has_war := true.B }
      }
    }
  }

  // Check dependencies against CTRL slot if active
  when(ctrl_active) {
    // LD_CU writes ctrl_rd
    when(ctrl_op === 5.U) {
      when(op === 3.U || op === 4.U) {
        when(rd === ctrl_rd || rs === ctrl_rd) { has_raw := true.B }
        when(rd === ctrl_rd) { has_waw := true.B }
      }.elsewhen(op === 1.U) {
        when(rs === ctrl_rd) { has_raw := true.B }
        when(rd === ctrl_rd) { has_waw := true.B }
      }.elsewhen(op === 2.U) {
        when(rd === ctrl_rd || rs === ctrl_rd) { has_raw := true.B }
      }
    }
  }

  val has_dep = has_raw || has_war || has_waw

  // Structural hazard check
  when(!has_dep) {
    when(op === 3.U || op === 4.U) {
      can_schedule := !alu_active
    }.elsewhen(op === 1.U || op === 2.U) {
      can_schedule := !lsu_active
    }.elsewhen(op === 5.U || op === 6.U || op === 7.U) {
      can_schedule := !ctrl_active
    }
  }

  switch(state) {
    is(sFetch) {
      io.mem.req  := true.B
      io.mem.addr := pc
      when(io.mem.ready) {
        when(can_schedule) {
          // Schedule instruction into packet
          when(op === 3.U || op === 4.U) {
            alu_active := true.B
            alu_op     := op
            alu_rd     := rd
            alu_rs     := rs
            pc         := pc + 1.U
          }.elsewhen(op === 1.U || op === 2.U) {
            lsu_active := true.B
            lsu_op     := op
            lsu_rd     := rd
            lsu_rs     := rs
            pc         := pc + 1.U
          }.otherwise { // CTRL: 5, 6, 7
            ctrl_active := true.B
            ctrl_op     := op
            ctrl_rd     := rd
            ctrl_target := target
            
            // Branch/Halt closes packet immediately
            when(op === 6.U || op === 7.U) {
              state := sExecute
            }.otherwise { // LD_CU closes packet or continues
              pc    := pc + 1.U
              state := sExecute
            }
          }
        }.otherwise {
          // Cannot schedule: execute current packet first, keep PC unchanged
          state := sExecute
        }
      }
    }

    is(sExecute) {
      when(lsu_active) {
        state := sExecuteLSU
      }.otherwise {
        // Execute ALU combinationally
        when(alu_active) {
          when(alu_op === 3.U) {
            regs(alu_rd) := regs(alu_rd) + regs(alu_rs)
          }.otherwise {
            regs(alu_rd) := regs(alu_rd) - regs(alu_rs)
          }
        }
        // Execute CTRL combinationally
        when(ctrl_active) {
          when(ctrl_op === 5.U) {
            regs(ctrl_rd) := ctrl_target
          }.elsewhen(ctrl_op === 6.U) {
            pc := ctrl_target
          }.elsewhen(ctrl_op === 7.U) {
            state := sHalt
          }
        }
        insts := insts + alu_active.asUInt + lsu_active.asUInt + ctrl_active.asUInt
        alu_active  := false.B
        lsu_active  := false.B
        ctrl_active := false.B
        
        when(ctrl_active && ctrl_op === 7.U) {
          state := sHalt
        }.otherwise {
          state := sFetch
        }
      }
    }

    is(sExecuteLSU) {
      io.mem.req := true.B
      when(lsu_op === 1.U) { // Load
        io.mem.addr := regs(lsu_rs)
        when(io.mem.ready) {
          regs(lsu_rd) := io.mem.rdata
          reads := reads + 1.U
          
          // Execute parallel ALU/CTRL
          when(alu_active) {
            when(alu_op === 3.U) {
              regs(alu_rd) := regs(alu_rd) + regs(alu_rs)
            }.otherwise {
              regs(alu_rd) := regs(alu_rd) - regs(alu_rs)
            }
          }
          when(ctrl_active) {
            when(ctrl_op === 5.U) {
              regs(ctrl_rd) := ctrl_target
            }.elsewhen(ctrl_op === 6.U) {
              pc := ctrl_target
            }.elsewhen(ctrl_op === 7.U) {
              state := sHalt
            }
          }
          insts := insts + alu_active.asUInt + lsu_active.asUInt + ctrl_active.asUInt
          alu_active  := false.B
          lsu_active  := false.B
          ctrl_active := false.B
          
          when(ctrl_active && ctrl_op === 7.U) {
            state := sHalt
          }.otherwise {
            state := sFetch
          }
        }
      }.otherwise { // Store (lsu_op === 2.U)
        io.mem.addr  := regs(lsu_rd)
        io.mem.write := true.B
        io.mem.wdata := regs(lsu_rs)
        when(io.mem.ready) {
          writes := writes + 1.U
          
          // Execute parallel ALU/CTRL
          when(alu_active) {
            when(alu_op === 3.U) {
              regs(alu_rd) := regs(alu_rd) + regs(alu_rs)
            }.otherwise {
              regs(alu_rd) := regs(alu_rd) - regs(alu_rs)
            }
          }
          when(ctrl_active) {
            when(ctrl_op === 5.U) {
              regs(ctrl_rd) := ctrl_target
            }.elsewhen(ctrl_op === 6.U) {
              pc := ctrl_target
            }.elsewhen(ctrl_op === 7.U) {
              state := sHalt
            }
          }
          insts := insts + alu_active.asUInt + lsu_active.asUInt + ctrl_active.asUInt
          alu_active  := false.B
          lsu_active  := false.B
          ctrl_active := false.B
          
          when(ctrl_active && ctrl_op === 7.U) {
            state := sHalt
          }.otherwise {
            state := sFetch
          }
        }
      }
    }
  }
}
