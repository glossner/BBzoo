package zoo.mitdataflow

import chisel3._
import chisel3.util._

class MitDataflowCtrlSignals extends Bundle {
  val opcode         = UInt(8.W)
  val node_id        = UInt(8.W)
  val slot           = UInt(8.W)
  val target_node_id = UInt(8.W)
  val is_load        = Bool()
  val is_store       = Bool()
  val is_add         = Bool()
  val is_hlt         = Bool()
}

class MitDataflowDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new MitDataflowCtrlSignals)
  })

  val op = io.inst(31, 24)

  io.ctrl.opcode         := op
  io.ctrl.node_id        := io.inst(23, 16)
  io.ctrl.slot           := io.inst(15, 8)
  io.ctrl.target_node_id := io.inst(23, 16)
  io.ctrl.is_load        := op === 0x40.U
  io.ctrl.is_add         := op === 0x41.U
  io.ctrl.is_store       := op === 0x42.U
  io.ctrl.is_hlt         := op === 0xFF.U
}
