package zoo.illiac4

import chisel3._
import chisel3.util._

class Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(16.W))
    
    val hlt       = Output(Bool())
    val ld_cu     = Output(Bool())
    val st_cu     = Output(Bool())
    val ldi_cu    = Output(Bool())
    val sti_cu    = Output(Bool())
    val jnz_cu    = Output(Bool())
    val jmp_cu    = Output(Bool())
    
    val ld_pe      = Output(Bool())
    val st_pe      = Output(Bool())
    val add_pe     = Output(Bool())
    val route_pe_l = Output(Bool())
    val route_pe_r = Output(Bool())
    val add_pe_reg = Output(Bool())
    
    val rd        = Output(UInt(2.W))
    val rs        = Output(UInt(2.W))
  })

  val op = io.inst(15, 12)
  io.rd := io.inst(9, 8)
  io.rs := io.inst(5, 4)

  io.hlt       := (io.inst === 0.U)
  io.ld_cu     := (op === 0x1.U)
  io.st_cu     := (op === 0x2.U)
  io.ldi_cu    := (op === 0x3.U)
  io.sti_cu    := (op === 0x4.U)
  io.jnz_cu    := (op === 0x5.U)
  io.jmp_cu    := (op === 0x6.U)
  io.ld_pe     := (op === 0x7.U)
  io.st_pe     := (op === 0x8.U)
  io.add_pe    := (op === 0x9.U)
  io.route_pe_l:= (op === 0xA.U)
  io.route_pe_r:= (op === 0xB.U)
  io.add_pe_reg:= (op === 0xC.U)
}
