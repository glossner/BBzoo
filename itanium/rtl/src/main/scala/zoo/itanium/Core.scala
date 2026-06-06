package zoo.itanium

import chisel3._
import chisel3.util._

class Core extends Module {
  val io = IO(new Bundle {
    val mem = new Bundle {
      val req   = Output(Bool())
      val addr  = Output(UInt(32.W))
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

  val sFetch0 :: sFetch1 :: sExecute :: sLSU :: sHalt :: Nil = Enum(5)
  val state = RegInit(sFetch0)

  // Registers: R0-R15 (64-bit)
  val regs      = RegInit(VecInit(Seq.fill(16)(0.U(64.W))))
  val regs_next = RegInit(VecInit(Seq.fill(16)(0.U(64.W))))

  // PC and bundle buffers
  val pc = RegInit(0.U(32.W))
  val tempVal0 = RegInit(0.U(64.W))
  val bundle   = RegInit(0.U(128.W))

  val slotIndex = RegInit(0.U(2.W))
  val jumpTaken = RegInit(false.B)
  val jumpTarget = RegInit(0.U(32.W))

  // PMU Counters
  val cycles = RegInit(0.U(64.W))
  val insts  = RegInit(0.U(64.W))
  val reads  = RegInit(0.U(64.W))
  val writes = RegInit(0.U(64.W))

  cycles := cycles + 1.U

  // Decoder
  val decoder = Module(new Decoder)
  decoder.io.inst := MuxLookup(slotIndex, 0.U)(Seq(
    0.U -> bundle(31, 0),
    1.U -> bundle(63, 32),
    2.U -> bundle(95, 64)
  ))

  val template = bundle(127, 96)
  val stopBits = Cat(template(10), template(9), template(8)) // bit 0 for slot 0, bit 1 for slot 1, bit 2 for slot 2
  val currentStop = stopBits(slotIndex)

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
        tempVal0 := io.mem.rdata
        pc       := pc + 2.U
        state    := sFetch1
      }
    }

    is(sFetch1) {
      io.mem.req  := true.B
      io.mem.addr := pc
      when(io.mem.ready) {
        bundle    := Cat(io.mem.rdata, tempVal0)
        pc        := pc + 2.U
        slotIndex := 0.U
        jumpTaken := false.B
        state     := sExecute
      }
    }

    is(sExecute) {
      assert(decoder.io.legal, "Unimplemented/illegal instruction")
      when(slotIndex === 3.U || (jumpTaken && slotIndex === 0.U)) {
        // End of bundle or branch taken
        when(jumpTaken) {
          pc := jumpTarget
        }
        state := sFetch0
      }.otherwise {
        val op = decoder.io.op
        val rd = decoder.io.rd(3, 0)
        val rs1 = decoder.io.rs1(3, 0)
        val rs2 = decoder.io.rs2(3, 0)
        val target = decoder.io.target

        when(op === 7.U) { // HLT
          insts := insts + 1.U
          state := sHalt
        }.elsewhen(op === 6.U) { // JMP
          jumpTarget := target
          jumpTaken  := true.B
          insts := insts + 1.U
          
          // Commit register file on JMP
          regs := regs_next
          slotIndex := 0.U // Stop executing subsequent slots
        }.elsewhen(op === 5.U) { // LD_CU rd = target
          regs_next(rd) := Cat(0.U(48.W), target)
          insts := insts + 1.U
          
          // Handle stop bit or end of bundle
          when(currentStop || slotIndex === 2.U) {
            regs := regs_next
          }
          slotIndex := slotIndex + 1.U
        }.elsewhen(op === 3.U) { // ADD rd = rs1, rs2
          regs_next(rd) := regs(rs1) + regs(rs2)
          insts := insts + 1.U
          
          when(currentStop || slotIndex === 2.U) {
            regs := regs_next
          }
          slotIndex := slotIndex + 1.U
        }.elsewhen(op === 4.U) { // SUB rd = rs1, rs2
          regs_next(rd) := regs(rs1) - regs(rs2)
          insts := insts + 1.U
          
          when(currentStop || slotIndex === 2.U) {
            regs := regs_next
          }
          slotIndex := slotIndex + 1.U
        }.elsewhen(op === 1.U || op === 2.U) { // LD / ST
          state := sLSU
        }.otherwise {
          // NOP
          when(currentStop || slotIndex === 2.U) {
            regs := regs_next
          }
          slotIndex := slotIndex + 1.U
        }
      }
    }

    is(sLSU) {
      val op = decoder.io.op
      val rd = decoder.io.rd(3, 0)
      val rs1 = decoder.io.rs1(3, 0)

      io.mem.req := true.B
      when(op === 1.U) { // ld8 rd = [rs1]
        io.mem.addr := regs(rs1)(31, 0)
        when(io.mem.ready) {
          regs_next(rd) := io.mem.rdata
          reads := reads + 1.U
          insts := insts + 1.U
          
          when(currentStop || slotIndex === 2.U) {
            regs := regs_next
          }
          slotIndex := slotIndex + 1.U
          state     := sExecute
        }
      }.otherwise { // st8 [rd] = rs1
        io.mem.addr  := regs(rd)(31, 0)
        io.mem.write := true.B
        io.mem.wdata := regs(rs1)
        when(io.mem.ready) {
          writes := writes + 1.U
          insts  := insts + 1.U
          
          when(currentStop || slotIndex === 2.U) {
            regs := regs_next
          }
          slotIndex := slotIndex + 1.U
          state     := sExecute
        }
      }
    }
  }
}
