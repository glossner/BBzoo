package zoo.burroughsb5500

import chisel3._
import chisel3.util._
import zoo.common.components.AluOp

class B5500CtrlSignals extends Bundle {
  val mem_read   = Bool()
  val mem_write  = Bool()
  val stack_push = Bool()
  val stack_pop  = Bool()
  val alu_op     = UInt(4.W)
  val is_hlt     = Bool()
  val legal    = Bool()
}

class B5500Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(48.W))
    val ctrl = Output(new B5500CtrlSignals)
    val addr = Output(UInt(15.W))
  })

  val opcode = io.inst(47, 40)
  io.addr   := io.inst(39, 25)

  // Default assignments
  io.ctrl.mem_read   := false.B
  io.ctrl.mem_write  := false.B
  io.ctrl.stack_push := false.B
  io.ctrl.stack_pop  := false.B
  io.ctrl.alu_op     := AluOp.PASS_A
  io.ctrl.is_hlt     := false.B

  io.ctrl.legal := false.B
  switch(opcode) {
    is(1.U) {
      io.ctrl.legal := true.B
       // PUSH
      io.ctrl.mem_read   := true.B
      io.ctrl.stack_push := true.B
    }
    is(2.U) {
      io.ctrl.legal := true.B
       // POP
      io.ctrl.mem_write  := true.B
      io.ctrl.stack_pop  := true.B
    }
    is(3.U) {
      io.ctrl.legal := true.B
       // ADD
      io.ctrl.stack_pop  := true.B
      io.ctrl.alu_op     := AluOp.ADD
    }
    is(4.U) {
      io.ctrl.legal := true.B
       // SUB
      io.ctrl.stack_pop  := true.B
      io.ctrl.alu_op     := AluOp.SUB
    }
    is(5.U) {
      io.ctrl.legal := true.B
       // HLT
      io.ctrl.is_hlt     := true.B
    }
  }
}
