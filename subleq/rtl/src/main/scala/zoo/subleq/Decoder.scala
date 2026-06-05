package zoo.subleq

import chisel3._

class SubleqCtrlSignals extends Bundle {
  val is_subleq = Bool()
}

class SubleqDecoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new SubleqCtrlSignals)
  })
  io.ctrl.is_subleq := true.B
}
