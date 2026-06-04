package zoo.cydra5

import chisel3._
import chisel3.util._

class Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(64.W))

    val alu_op   = Output(UInt(4.W))
    val alu_rd   = Output(UInt(3.W))
    val alu_rs1  = Output(UInt(3.W))
    val alu_rs2  = Output(UInt(3.W))

    val lsu_op   = Output(UInt(4.W))
    val lsu_reg  = Output(UInt(3.W))
    val lsu_base = Output(UInt(3.W))

    val ctrl_op  = Output(UInt(4.W))
    val target   = Output(UInt(16.W))
  })

  // Word 0 (bits 63-32)
  val w0 = io.inst(63, 32)
  io.alu_op  := w0(28, 25)
  io.alu_rd  := w0(24, 22)
  io.alu_rs1 := w0(21, 19)
  io.alu_rs2 := w0(18, 16)

  io.lsu_op   := w0(9, 6)
  io.lsu_reg  := w0(5, 3)
  io.lsu_base := w0(2, 0)

  // Word 1 (bits 31-0)
  val w1 = io.inst(31, 0)
  io.ctrl_op := w1(23, 20)
  io.target  := w1(15, 0)
  // Maps rd to alu_rs1 during LD_CU (opcode 3)
  io.alu_rs1 := Mux(w1(23, 20) === 3.U, w1(19, 16), w0(21, 19))
}
