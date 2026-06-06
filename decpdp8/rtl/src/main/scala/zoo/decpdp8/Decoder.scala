package zoo.decpdp8

import chisel3._
import chisel3.util._
import zoo.common.components.AluOp

class Pdp8CtrlSignals extends Bundle {
  val mem_read   = Bool()
  val mem_write  = Bool()
  val alu_op     = UInt(4.W)
  val alu_src_b  = Bool() // false = Constant/None, true = Mem
  
  // Accumulator controls
  val acc_load   = Bool()
  val acc_clear  = Bool()
  val acc_comp   = Bool()
  val acc_rot_l  = Bool()
  val acc_rot_r  = Bool()
  
  // Link controls
  val link_clear = Bool()
  val link_comp  = Bool()
  val link_load  = Bool()
  val link_in    = Bool()
  
  // Control flow
  val is_jmp     = Bool()
  val is_jms     = Bool()
  val is_isz     = Bool()
  val is_opr     = Bool()
  val is_iot     = Bool()
  val legal    = Bool()
}

/**
 * DEC PDP-8 Instruction Decoder.
 * Decodes the 12-bit instruction into control signals and evaluates Group 2 skip conditions.
 */
class Pdp8Decoder extends Module {
  val io = IO(new Bundle {
    val inst     = Input(UInt(12.W))
    val ctrl     = Output(new Pdp8CtrlSignals)
    
    // For OPR Group 2 skip evaluation
    val acc_val  = Input(UInt(12.W))
    val link_val = Input(Bool())
    val skip     = Output(Bool())
  })

  val opcode   = io.inst(11, 9)

  // Default control signals
  io.ctrl.mem_read   := false.B
  io.ctrl.mem_write  := false.B
  io.ctrl.alu_op     := AluOp.PASS_A
  io.ctrl.alu_src_b  := false.B
  io.ctrl.acc_load   := false.B
  io.ctrl.acc_clear  := false.B
  io.ctrl.acc_comp   := false.B
  io.ctrl.acc_rot_l  := false.B
  io.ctrl.acc_rot_r  := false.B
  io.ctrl.link_clear := false.B
  io.ctrl.link_comp  := false.B
  io.ctrl.link_load  := false.B
  io.ctrl.link_in    := false.B
  io.ctrl.is_jmp     := false.B
  io.ctrl.is_jms     := false.B
  io.ctrl.is_isz     := false.B
  io.ctrl.is_opr     := false.B
  io.ctrl.is_iot     := false.B
  io.skip            := false.B

  switch(opcode) {
    is(0.U) { // AND
      io.ctrl.mem_read  := true.B
      io.ctrl.alu_op    := AluOp.AND
      io.ctrl.alu_src_b := true.B
      io.ctrl.acc_load  := true.B
    }
    is(1.U) { // TAD (Two's Complement Add)
      io.ctrl.mem_read   := true.B
      io.ctrl.alu_op     := AluOp.ADD
      io.ctrl.alu_src_b  := true.B
      io.ctrl.acc_load   := true.B
      io.ctrl.link_load  := true.B // Carry out goes to Link
    }
    is(2.U) { // ISZ (Increment and Skip if Zero)
      io.ctrl.mem_read  := true.B
      io.ctrl.mem_write := true.B
      io.ctrl.is_isz    := true.B
    }
    is(3.U) { // DCA (Deposit and Clear Accumulator)
      io.ctrl.mem_write := true.B
      io.ctrl.acc_clear := true.B
    }
    is(4.U) { // JMS (Jump to Subroutine)
      io.ctrl.mem_write := true.B
      io.ctrl.is_jms    := true.B
    }
    is(5.U) { // JMP (Jump)
      io.ctrl.is_jmp    := true.B
    }
    is(6.U) { // IOT (Input/Output Transfer)
      io.ctrl.is_iot    := true.B
    }
    is(7.U) { // OPR (Operate)
      io.ctrl.is_opr    := true.B
      val is_group2 = io.inst(8)
      
      when(!is_group2) {
        // Group 1
        val cla = io.inst(7)
        val cll = io.inst(6)
        val cma = io.inst(5)
        val cml = io.inst(4)
        val rar = io.inst(3)
        val ral = io.inst(2)
        val iac = io.inst(0)

        io.ctrl.acc_clear := cla
        io.ctrl.link_clear := cll
        io.ctrl.acc_comp := cma
        io.ctrl.link_comp := cml
        
        when(rar) {
          io.ctrl.acc_rot_r := true.B
        }.elsewhen(ral) {
          io.ctrl.acc_rot_l := true.B
        }

        when(iac) {
          io.ctrl.alu_op := AluOp.ADD
          io.ctrl.acc_load := true.B
        }
      }.otherwise {
        // Group 2
        val cla = io.inst(7)
        val sma = io.inst(6)
        val sza = io.inst(5)
        val snl = io.inst(4)
        val rev = io.inst(3)

        io.ctrl.acc_clear := cla
        
        // Skip condition evaluation
        val is_neg = io.acc_val(11) === 1.U
        val is_zero = io.acc_val === 0.U
        val link_nonzero = io.link_val

        val skip_cond = Wire(Bool())
        when(!rev) {
          // OR group: skip if any selected condition is true
          skip_cond := (sma && is_neg) || (sza && is_zero) || (snl && link_nonzero)
        }.otherwise {
          // AND group (reverse sense): skip if ALL selected conditions are false
          val cond = (sma && is_neg) || (sza && is_zero) || (snl && link_nonzero)
          skip_cond := !cond
        }
        
        io.skip := skip_cond
      }
    }
  }

  io.ctrl.legal := true.B
}
