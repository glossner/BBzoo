package zoo.ibmmfast

import chisel3._
import chisel3.util._

class Decoder extends Module {
  val io = IO(new Bundle {
    val legal = Output(Bool())

    val inst = Input(UInt(32.W))
    
    val is_vliw      = Output(Bool())
    val is_jmp       = Output(Bool())
    val is_hlt       = Output(Bool())
    val is_ld_cu     = Output(Bool())
    
    val alu_op       = Output(UInt(2.W))
    val alu_rd       = Output(UInt(3.W))
    val alu_rs1      = Output(UInt(3.W))
    val alu_rs2      = Output(UInt(2.W))
    
    val mau_op       = Output(UInt(2.W))
    val mau_rd       = Output(UInt(3.W))
    val mau_rs1      = Output(UInt(3.W))
    val mau_rs2      = Output(UInt(2.W))
    
    val lsu_op       = Output(UInt(2.W))
    val lsu_reg      = Output(UInt(3.W))
    val lsu_base     = Output(UInt(5.W))
    
    val target_addr  = Output(UInt(16.W))
    val cu_rd        = Output(UInt(2.W))
  })

  val ctrl = io.inst(31, 30)

  io.is_vliw  := (ctrl === 0.U)
  io.is_jmp   := (ctrl === 1.U)
  io.is_hlt   := (ctrl === 2.U)
  io.is_ld_cu := (ctrl === 3.U)

  io.alu_op   := io.inst(29, 28)
  io.alu_rd   := io.inst(27, 25)
  io.alu_rs1  := io.inst(24, 22)
  io.alu_rs2  := io.inst(21, 20)

  io.mau_op   := io.inst(19, 18)
  io.mau_rd   := io.inst(17, 15)
  io.mau_rs1  := io.inst(14, 12)
  io.mau_rs2  := io.inst(11, 10)

  io.lsu_op   := io.inst(9, 8)
  io.lsu_reg  := io.inst(7, 5)
  io.lsu_base := io.inst(4, 0)

  io.target_addr := io.inst(15, 0)
  io.cu_rd       := io.inst(25, 24)

  io.legal := io.is_vliw || io.is_jmp || io.is_hlt || io.is_ld_cu
}
