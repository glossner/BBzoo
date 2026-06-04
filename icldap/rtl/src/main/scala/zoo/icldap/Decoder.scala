package zoo.icldap

import chisel3._
import chisel3.util._

class Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(16.W))
    
    val hlt        = Output(Bool())
    val ld_cu      = Output(Bool())
    val st_cu      = Output(Bool())
    val ld_pe_bit  = Output(Bool())
    val add_pe_bit = Output(Bool())
    val st_pe_bit  = Output(Bool())
    val clr_carry  = Output(Bool())
    
    val rd         = Output(UInt(2.W))
    val rs         = Output(UInt(2.W))
    val bit        = Output(UInt(4.W))
  })

  val op = io.inst(15, 12)
  io.rd := io.inst(9, 8)
  io.rs := io.inst(5, 4)
  io.bit := io.inst(3, 0)

  io.hlt        := (io.inst === 0.U)
  io.ld_cu      := (op === 0x1.U)
  io.st_cu      := (op === 0x2.U)
  io.ld_pe_bit  := (op === 0x3.U)
  io.add_pe_bit := (op === 0x4.U)
  io.st_pe_bit  := (op === 0x5.U)
  io.clr_carry  := (op === 0x6.U)
}
