package zoo.common.components

import chisel3._

/**
 * Standard simple memory bus interface for the Zoo processors.
 */
class SimpleMemIO(val addrWidth: Int, val dataWidth: Int) extends Bundle {
  val req   = Output(Bool())
  val addr  = Output(UInt(addrWidth.W))
  val write = Output(Bool())
  val wdata = Output(UInt(dataWidth.W))
  val rdata = Input(UInt(dataWidth.W))
  val ready = Input(Bool())
}
