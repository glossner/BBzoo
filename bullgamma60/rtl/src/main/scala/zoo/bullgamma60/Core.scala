package zoo.bullgamma60

import chisel3._
import chisel3.util._
import zoo.common.components._

/**
 * Bull Gamma 60 Core Processor.
 * A 24-bit multithreaded time-multiplexed CPU core.
 */
class Bullgamma60Core extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 16, dataWidth = 24)
    val hlt = Output(Bool())

    // Debug ports
    val pc_debug   = Output(UInt(16.W))
    val acc0_debug = Output(UInt(24.W))
    val acc1_debug = Output(UInt(24.W))

    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  // Sub-modules
  val decoder = Module(new Bullgamma60Decoder)

  // Core Registers
  val pc0           = RegInit(0.U(16.W))
  val pc1           = RegInit(0.U(16.W))
  val active1       = RegInit(false.B)
  val currentThread = RegInit(0.U(1.W)) // 0: Thread 0, 1: Thread 1

  val acc0          = RegInit(0.U(24.W))
  val acc1          = RegInit(0.U(24.W))
  val joinWaiting   = RegInit(false.B)
  val hltMain       = RegInit(false.B)
  val hltReg        = RegInit(false.B)
  val inst          = RegInit(0.U(24.W))

  // Debug outputs
  io.hlt        := hltReg
  io.pc_debug   := Mux(currentThread === 0.U, pc0, pc1)
  io.acc0_debug := acc0
  io.acc1_debug := acc1

  // Decoder inputs
  decoder.io.inst := inst
  val targetReg = decoder.io.reg
  val data_addr = decoder.io.addr

  // FSM States
  val sFETCH :: sDECODE :: sMEM_READ :: sMEM_WRITE :: Nil = Enum(4)
  val state = RegInit(sFETCH)

  // Default IO assignments
  io.mem.req   := false.B
  io.mem.addr  := 0.U
  io.mem.write := false.B
  io.mem.wdata := 0.U

  // FSM Logic
  switch(state) {
    is(sFETCH) {
      when(hltMain && !active1) {
        hltReg := true.B
      }.otherwise {
        when(currentThread === 0.U) {
          when(joinWaiting && !active1) {
            joinWaiting := false.B
            io.mem.req  := true.B
            io.mem.addr := pc0
            when(io.mem.ready) {
              inst  := io.mem.rdata
              state := sDECODE
            }
          }.elsewhen(hltMain || joinWaiting) {
            when(active1) {
              currentThread := 1.U
            }
          }.otherwise {
            io.mem.req  := true.B
            io.mem.addr := pc0
            when(io.mem.ready) {
              inst  := io.mem.rdata
              state := sDECODE
            }
          }
        }.otherwise {
          // Thread 1
          when(active1) {
            io.mem.req  := true.B
            io.mem.addr := pc1
            when(io.mem.ready) {
              inst  := io.mem.rdata
              state := sDECODE
            }
          }.otherwise {
            currentThread := 0.U
          }
        }
      }
    }

    is(sDECODE) {
      assert(decoder.io.ctrl.legal, "Unimplemented/illegal instruction")
      val ctrl = decoder.io.ctrl
      when(currentThread === 0.U) {
        when(ctrl.is_fork) {
          active1       := true.B
          pc1           := data_addr
          pc0           := pc0 + 1.U
          currentThread := 1.U
          state         := sFETCH
        }.elsewhen(ctrl.is_join) {
          when(active1) {
            joinWaiting   := true.B
            currentThread := 1.U
          }.otherwise {
            pc0 := pc0 + 1.U
          }
          state         := sFETCH
        }.elsewhen(ctrl.is_hlt) {
          hltMain       := true.B
          currentThread := Mux(active1, 1.U, 0.U)
          state         := sFETCH
        }.elsewhen(ctrl.mem_read) {
          state         := sMEM_READ
        }.elsewhen(ctrl.mem_write) {
          state         := sMEM_WRITE
        }.otherwise {
          pc0           := pc0 + 1.U
          currentThread := Mux(active1, 1.U, 0.U)
          state         := sFETCH
        }
      }.otherwise {
        // Thread 1
        when(ctrl.is_join) {
          active1       := false.B
          currentThread := 0.U
          state         := sFETCH
        }.elsewhen(ctrl.mem_read) {
          state         := sMEM_READ
        }.elsewhen(ctrl.mem_write) {
          state         := sMEM_WRITE
        }.otherwise {
          pc1           := pc1 + 1.U
          currentThread := 0.U
          state         := sFETCH
        }
      }
    }

    is(sMEM_READ) {
      io.mem.req   := true.B
      io.mem.addr  := data_addr
      io.mem.write := false.B
      when(io.mem.ready) {
        val rdata = io.mem.rdata
        when(currentThread === 0.U) {
          when(targetReg === 0.U) {
            acc0 := Mux(decoder.io.ctrl.is_ld, rdata, acc0 + rdata)
          }.otherwise {
            acc1 := Mux(decoder.io.ctrl.is_ld, rdata, acc1 + rdata)
          }
          pc0           := pc0 + 1.U
          currentThread := Mux(active1, 1.U, 0.U)
        }.otherwise {
          when(targetReg === 0.U) {
            acc0 := Mux(decoder.io.ctrl.is_ld, rdata, acc0 + rdata)
          }.otherwise {
            acc1 := Mux(decoder.io.ctrl.is_ld, rdata, acc1 + rdata)
          }
          pc1           := pc1 + 1.U
          currentThread := 0.U
        }
        state := sFETCH
      }
    }

    is(sMEM_WRITE) {
      io.mem.req   := true.B
      io.mem.addr  := data_addr
      io.mem.write := true.B
      io.mem.wdata := Mux(targetReg === 0.U, acc0, acc1)
      when(io.mem.ready) {
        when(currentThread === 0.U) {
          pc0           := pc0 + 1.U
          currentThread := Mux(active1, 1.U, 0.U)
        }.otherwise {
          pc1           := pc1 + 1.U
          currentThread := 0.U
        }
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
  when(state === sFETCH && io.mem.ready && !(hltMain && !active1)) {
    pmu_insts := pmu_insts + 1.U
  }

  io.pmu_cycles := pmu_cycles
  io.pmu_insts  := pmu_insts
  io.pmu_reads  := pmu_reads
  io.pmu_writes := pmu_writes
}
