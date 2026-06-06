package zoo.convexc1

import chisel3._
import chisel3.util._

class Decoder extends Module {
  val io = IO(new Bundle {
    val legal = Output(Bool())

    val inst   = Input(UInt(32.W))
    val op     = Output(UInt(4.W))
    val rd     = Output(UInt(3.W))
    val rs1    = Output(UInt(3.W))
    val rs2    = Output(UInt(3.W))
    val target = Output(UInt(16.W))
  })

  io.op     := io.inst(31, 28)
  io.rd     := io.inst(26, 24) // Bits 26-24 for 3-bit GPR or Vector register index
  io.rs1    := io.inst(22, 20) // Bits 22-20
  io.rs2    := io.inst(18, 16) // Bits 18-16
  io.target := io.inst(15, 0)  // Bits 15-0

  io.legal := (io.op === 0.U || io.op === 1.U || io.op === 2.U || io.op === 3.U || io.op === 4.U || io.op === 5.U || io.op === 6.U || io.op === 7.U || io.op === 8.U)
}
