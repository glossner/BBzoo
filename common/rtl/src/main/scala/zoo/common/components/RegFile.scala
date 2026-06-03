package zoo.common.components

import chisel3._
import chisel3.util._

/**
 * Parameterized Register File with 2 Read Ports and 1 Write Port.
 * Used for General Register (GPR) Architectures.
 */
class GeneralRegFile(val numRegs: Int, val width: Int) extends Module {
  val io = IO(new Bundle {
    val rs1_addr = Input(UInt(log2Ceil(numRegs).W))
    val rs2_addr = Input(UInt(log2Ceil(numRegs).W))
    val rd_addr  = Input(UInt(log2Ceil(numRegs).W))
    val rd_data  = Input(UInt(width.W))
    val wen      = Input(Bool())
    
    val rs1_data = Output(UInt(width.W))
    val rs2_data = Output(UInt(width.W))

    // Debug output to expose register state to testbenches
    val regs_debug = Output(Vec(numRegs, UInt(width.W)))
  })

  // Initialize all registers to 0
  val regs = RegInit(VecInit(Seq.fill(numRegs)(0.U(width.W))))

  io.rs1_data := regs(io.rs1_addr)
  io.rs2_data := regs(io.rs2_addr)
  io.regs_debug := regs

  when(io.wen) {
    regs(io.rd_addr) := io.rd_data
  }
}

/**
 * Accumulator Register with optional Link/Carry Bit.
 * Used for Accumulator-based architectures (e.g. PDP-8).
 */
class AccumulatorReg(val width: Int, val hasLinkBit: Boolean) extends Module {
  val io = IO(new Bundle {
    val in_data  = Input(UInt(width.W))
    val load     = Input(Bool())
    
    // Accumulator Operations
    val clear    = Input(Bool())
    val complement = Input(Bool())
    val rotate_left = Input(Bool())
    val rotate_right = Input(Bool())
    
    val out_data = Output(UInt(width.W))
    
    // Link/Carry bit logic
    val link_in   = Input(Bool())
    val link_load = Input(Bool())
    val link_comp = Input(Bool())
    val link_clear = Input(Bool())
    val link_out  = Output(Bool())
  })

  val acc = RegInit(0.U(width.W))
  val link = if (hasLinkBit) RegInit(false.B) else 0.B

  // Link bit operations
  if (hasLinkBit) {
    when (io.link_clear) {
      link := false.B
    } .elsewhen (io.link_comp) {
      link := !link
    } .elsewhen (io.link_load) {
      link := io.link_in
    } .elsewhen (io.rotate_left) {
      link := acc(width - 1)
    } .elsewhen (io.rotate_right) {
      link := acc(0)
    }

    // Accumulator register operations
    when (io.rotate_left) {
      acc := Cat(acc(width-2, 0), link)
    } .elsewhen (io.rotate_right) {
      acc := Cat(link, acc(width-1, 1))
    } .elsewhen (io.clear) {
      acc := 0.U
    } .elsewhen (io.complement) {
      acc := ~acc
    } .elsewhen (io.load) {
      acc := io.in_data
    }
    
    io.link_out := link
  } else {
    when (io.clear) {
      acc := 0.U
    } .elsewhen (io.complement) {
      acc := ~acc
    } .elsewhen (io.rotate_left) {
      acc := Cat(acc(width-2, 0), acc(width-1))
    } .elsewhen (io.rotate_right) {
      acc := Cat(acc(0), acc(width-1, 1))
    } .elsewhen (io.load) {
      acc := io.in_data
    }
    io.link_out := false.B
  }

  io.out_data := acc
}

/**
 * Synthesizable LIFO Stack structure.
 * Used for Stack-based architectures (e.g. Burroughs B5500).
 */
class StackMemory(val depth: Int, val width: Int) extends Module {
  val io = IO(new Bundle {
    val push = Input(Bool())
    val pop  = Input(Bool())
    val data_in = Input(UInt(width.W))
    val data_out = Output(UInt(width.W))
    
    val empty = Output(Bool())
    val full  = Output(Bool())
  })

  val stackMem = Mem(depth, UInt(width.W))
  val sp = RegInit(0.U(log2Ceil(depth + 1).W))

  io.empty := sp === 0.U
  io.full  := sp === depth.U

  when(io.push && !io.full) {
    stackMem(sp) := io.data_in
    sp := sp + 1.U
  }.elsewhen(io.pop && !io.empty) {
    sp := sp - 1.U
  }

  // Top of stack is at sp - 1
  io.data_out := Mux(io.empty, 0.U, stackMem(sp - 1.U))
}
