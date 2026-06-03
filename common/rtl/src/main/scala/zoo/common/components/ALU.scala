package zoo.common.components

import chisel3._
import chisel3.util._

object AluOp {
  val ADD    = 0.U(4.W)
  val SUB    = 1.U(4.W)
  val AND    = 2.U(4.W)
  val OR     = 3.U(4.W)
  val XOR    = 4.U(4.W)
  val SHL    = 5.U(4.W)
  val SHR    = 6.U(4.W)
  val NOT    = 7.U(4.W)
  val PASS_A = 8.U(4.W)
  val PASS_B = 9.U(4.W)
}

/**
 * Parameterized ALU for N-bit operands.
 */
class ParameterizedALU(val width: Int) extends Module {
  val io = IO(new Bundle {
    val a         = Input(UInt(width.W))
    val b         = Input(UInt(width.W))
    val op        = Input(UInt(4.W))
    val out       = Output(UInt(width.W))
    val carry_out = Output(Bool())
    val zero      = Output(Bool())
  })

  val sum  = Wire(UInt((width + 1).W))
  val diff = Wire(UInt((width + 1).W))

  sum  := io.a +& io.b
  diff := io.a -& io.b

  val res = WireInit(0.U(width.W))
  val carry = WireInit(false.B)

  // Avoid log2Ceil(width) error when width is 1 (unlikely, but safe)
  val shiftAmountWidth = if (width > 1) log2Ceil(width) else 1

  switch(io.op) {
    is(AluOp.ADD) {
      res := sum(width - 1, 0)
      carry := sum(width)
    }
    is(AluOp.SUB) {
      res := diff(width - 1, 0)
      carry := diff(width)
    }
    is(AluOp.AND)    { res := io.a & io.b }
    is(AluOp.OR)     { res := io.a | io.b }
    is(AluOp.XOR)    { res := io.a ^ io.b }
    is(AluOp.SHL)    { res := io.a << io.b(shiftAmountWidth - 1, 0) }
    is(AluOp.SHR)    { res := io.a >> io.b(shiftAmountWidth - 1, 0) }
    is(AluOp.NOT)    { res := ~io.a }
    is(AluOp.PASS_A) { res := io.a }
    is(AluOp.PASS_B) { res := io.b }
  }

  io.out := res
  io.carry_out := carry
  io.zero := res === 0.U
}
