package zoo.iram

import chisel3._
import chisel3.util._
import zoo.common.components._

class IramCore extends Module {
  val io = IO(new Bundle {
    val mem = new SimpleMemIO(addrWidth = 32, dataWidth = 32)
    val hlt = Output(Bool())
    
    // Debug ports
    val pc_debug   = Output(UInt(32.W))
    val regs_debug = Output(Vec(32, UInt(32.W)))
    
    // PMU outputs
    val pmu_cycles = Output(UInt(32.W))
    val pmu_insts  = Output(UInt(32.W))
    val pmu_reads  = Output(UInt(32.W))
    val pmu_writes = Output(UInt(32.W))
  })

  val decoder = Module(new IramDecoder)

  // Register files
  val scalar_regs = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))
  val vector_regs = RegInit(VecInit(Seq.fill(8)(VecInit(Seq.fill(4)(0.U(32.W))))))
  val pc          = RegInit(0.U(32.W))
  val hltReg      = RegInit(false.B)

  io.hlt      := hltReg
  io.pc_debug := pc
  for (i <- 0 until 32) {
    io.regs_debug(i) := scalar_regs(i)
  }

  val inst        = RegInit(0.U(32.W))
  val target_addr = RegInit(0.U(32.W))
  val element_idx = RegInit(0.U(2.W))

  decoder.io.inst := inst

  val sFETCH :: sDECODE :: sFETCH_ADDR :: sLOAD_VLD :: sSTORE_VST :: sLOAD_SCALAR :: sSTORE_SCALAR :: Nil = Enum(7)
  val state = RegInit(sFETCH)

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
      assert(decoder.io.ctrl.legal, "Unimplemented/illegal instruction")
      when(decoder.io.ctrl.is_hlt) {
        hltReg := true.B
        state  := sFETCH
      }.elsewhen(decoder.io.ctrl.is_vadd) {
        val vd = decoder.io.ctrl.rd(2, 0)
        val va = decoder.io.ctrl.rs1(2, 0)
        val jb = decoder.io.ctrl.rs2(2, 0)
        for (i <- 0 until 4) {
          vector_regs(vd)(i) := vector_regs(va)(i) + vector_regs(jb)(i)
        }
        state := sFETCH
      }.elsewhen(decoder.io.ctrl.is_vld || decoder.io.ctrl.is_vst) {
        element_idx := 0.U
        state       := Mux(decoder.io.ctrl.is_vld, sLOAD_VLD, sSTORE_VST)
      }.elsewhen(decoder.io.ctrl.is_lw || decoder.io.ctrl.is_sw) {
        state       := sFETCH_ADDR
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
        state       := Mux(decoder.io.ctrl.is_sw, sSTORE_SCALAR, sLOAD_SCALAR)
      }
    }

    is(sLOAD_VLD) {
      val base_addr = scalar_regs(decoder.io.ctrl.rs1)
      io.mem.req   := true.B
      io.mem.addr  := base_addr + element_idx
      io.mem.write := false.B
      when(io.mem.ready) {
        vector_regs(decoder.io.ctrl.rd(2, 0))(element_idx) := io.mem.rdata
        element_idx := element_idx + 1.U
        when(element_idx === 3.U) {
          state := sFETCH
        }
      }
    }

    is(sSTORE_VST) {
      val base_addr = scalar_regs(decoder.io.ctrl.rs1)
      io.mem.req   := true.B
      io.mem.addr  := base_addr + element_idx
      io.mem.write := true.B
      io.mem.wdata := vector_regs(decoder.io.ctrl.rd(2, 0))(element_idx)
      when(io.mem.ready) {
        element_idx := element_idx + 1.U
        when(element_idx === 3.U) {
          state := sFETCH
        }
      }
    }

    is(sLOAD_SCALAR) {
      io.mem.req   := true.B
      io.mem.addr  := target_addr
      io.mem.write := false.B
      when(io.mem.ready) {
        scalar_regs(decoder.io.ctrl.rd) := io.mem.rdata
        state := sFETCH
      }
    }

    is(sSTORE_SCALAR) {
      io.mem.req   := true.B
      io.mem.addr  := target_addr
      io.mem.write := true.B
      io.mem.wdata := scalar_regs(decoder.io.ctrl.rd)
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
