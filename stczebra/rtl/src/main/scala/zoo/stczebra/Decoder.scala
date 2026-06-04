package zoo.stczebra

import chisel3._
import chisel3.util._

class StczebraCtrlSignals extends Bundle {
  val clear_acc = Bool()
  val mem_read  = Bool()
  val mem_write = Bool()
  val is_add    = Bool()
  val is_hlt    = Bool()
}

class StczebraDecoder extends Module {
  val io = IO(new Bundle {
    val inst      = Input(UInt(33.W))
    val ctrl      = Output(new StczebraCtrlSignals)
    val data_addr = Output(UInt(16.W))
  })

  // ZEBRA uses functional bits rather than a standard opcode:
  // Bit 32: C (Clear Accumulator)
  // Bit 31: R (Read Memory)
  // Bit 30: W (Write Memory)
  // Bit 29: A (Add operation)
  // Bit 28: H (Halt)
  // Bits 5-17: Drum Address
  // Bits 0-4: Register Address (unused in simple core)
  
  io.ctrl.clear_acc := io.inst(32)
  io.ctrl.mem_read  := io.inst(31)
  io.ctrl.mem_write := io.inst(30)
  io.ctrl.is_add    := io.inst(29)
  io.ctrl.is_hlt    := io.inst(28)
  
  io.data_addr      := io.inst(17, 5)
}
