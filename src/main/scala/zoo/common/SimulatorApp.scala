package zoo.common

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import scala.io.Source
import java.io.File
import zoo.common.components.SimpleMemIO

class CoreWrapper(val m: Module) {
  val io = m.getClass.getMethod("io").invoke(m)

  def mem: SimpleMemIO = io.getClass.getMethod("mem").invoke(io).asInstanceOf[SimpleMemIO]
  def hlt: Bool = io.getClass.getMethod("hlt").invoke(io).asInstanceOf[Bool]

  def pmu_cycles: UInt = io.getClass.getMethod("pmu_cycles").invoke(io).asInstanceOf[UInt]
  def pmu_insts: UInt = io.getClass.getMethod("pmu_insts").invoke(io).asInstanceOf[UInt]
  def pmu_reads: UInt = io.getClass.getMethod("pmu_reads").invoke(io).asInstanceOf[UInt]
  def pmu_writes: UInt = io.getClass.getMethod("pmu_writes").invoke(io).asInstanceOf[UInt]

  def getDebugPort(name: String): Option[Data] = {
    try {
      val method = io.getClass.getMethod(name)
      Some(method.invoke(io).asInstanceOf[Data])
    } catch {
      case _: NoSuchMethodException => None
    }
  }

  def peekVal(data: Data): BigInt = {
    data match {
      case b: Bool => if (b.peek().litToBoolean) 1 else 0
      case u: UInt => u.peek().litValue
      case other => other.asInstanceOf[Bits].peek().litValue
    }
  }
}

object SimulatorApp {
  def main(args: Array[String]): Unit = {
    var arch = ""
    var hexPath = ""
    var maxCycles = 500
    var trace = false

    var i = 0
    while (i < args.length) {
      args(i) match {
        case "--arch" =>
          arch = args(i + 1)
          i += 2
        case "--hex" =>
          hexPath = args(i + 1)
          i += 2
        case "--cycles" =>
          maxCycles = args(i + 1).toInt
          i += 2
        case "--trace" =>
          trace = true
          i += 1
        case other =>
          println(s"Warning: unknown option $other")
          i += 1
      }
    }

    if (arch.isEmpty || hexPath.isEmpty) {
      printHelp()
      sys.exit(1)
    }

    runSimulation(arch, hexPath, maxCycles, trace)
  }

  def printHelp(): Unit = {
    println("BBZoo Standalone Core Simulator")
    println("Usage: sbt \"run --arch <arch_name> --hex <hex_file> [--trace] [--cycles <limit>]\"")
    println("\nSupported architectures:")
    println("  babbage, harvard, zuse, manchestermu1, univac, princetonias, cambridgeedsac,")
    println("  ibm701, ibm704, ibm650, ibm705, ibm1401, stczebra, bullgamma, ibmstretch,")
    println("  mos, pdp8, pdp11, ibm360, m68k, b5500, cdc, cray, univac1103a, cdc6600ppu,")
    println("  decvax, intel8080a, motorola6800, ibm6150, mips1, arm1, berkeleyrisc,")
    println("  hp3000, ethlilith, ucsdp, upd7720, tms32010, adsp2100, ibmmwave,")
    println("  voodoo1, geforce256, radeonr100, powervr1, mali200, amdr600")
  }

  def runSimulation(arch: String, hexPath: String, maxCycles: Int, trace: Boolean): Unit = {
    val file = new File(hexPath)
    if (!file.exists()) {
      println(s"Error: Hex file $hexPath does not exist!")
      sys.exit(1)
    }

    val lines = Source.fromFile(file).getLines()
      .map(_.trim)
      .filter(l => l.nonEmpty && !l.startsWith("#"))
      .toArray

    println(s"[Simulator] Loaded ${lines.length} words of program.")
    println(s"[Simulator] Initializing core: $arch")

    // Helper run closure to generalize simulation
    def runGeneric[T <: Module](coreGen: => T, wordWidth: Int, memorySize: Int): Unit = {
      val mem = Array.fill(memorySize)(BigInt(0))
      for (idx <- lines.indices) {
        mem(idx) = BigInt(lines(idx), 16)
      }
      val initialMem = mem.clone()

      simulate(coreGen) { c =>
        val wrapper = new CoreWrapper(c)
        wrapper.mem.ready.poke(false.B)
        wrapper.mem.rdata.poke(0.U)
        c.reset.poke(true.B)
        c.clock.step(5)
        c.reset.poke(false.B)

        var cycle = 0
        var halted = false
        while (cycle < maxCycles && !halted) {
          val req   = wrapper.mem.req.peek().litToBoolean
          val addr  = wrapper.mem.addr.peek().litValue.toInt
          val write = wrapper.mem.write.peek().litToBoolean
          val wdata = wrapper.mem.wdata.peek().litValue

          if (req) {
            wrapper.mem.ready.poke(true.B)
            if (write) {
              if (addr >= 0 && addr < memorySize) {
                mem(addr) = wdata
              }
            }
            if (addr >= 0 && addr < memorySize) {
              wrapper.mem.rdata.poke(mem(addr).U)
            } else {
              wrapper.mem.rdata.poke(0.U)
            }
          } else {
            wrapper.mem.ready.poke(false.B)
          }

          c.clock.step(1)
          cycle += 1

          if (trace) {
            val pcOpt = wrapper.getDebugPort("pc_debug").orElse(wrapper.getDebugPort("debug_pc")).map(wrapper.peekVal)
            val pcStr = pcOpt.map(v => s" pc=$v").getOrElse("")
            val reqStr = if (req) s" [Mem: ${if (write) "Write" else "Read"} Addr=$addr Val=${if (write) wdata else mem(addr)}]" else ""

            val r0 = wrapper.getDebugPort("r0_debug").orElse(wrapper.getDebugPort("debug_r0")).map(wrapper.peekVal).map(_.toString).getOrElse("-")
            val r1 = wrapper.getDebugPort("r1_debug").orElse(wrapper.getDebugPort("debug_r1")).map(wrapper.peekVal).map(_.toString).getOrElse("-")
            val r2 = wrapper.getDebugPort("r2_debug").orElse(wrapper.getDebugPort("debug_r2")).map(wrapper.peekVal).map(_.toString).getOrElse("-")
            val r3 = wrapper.getDebugPort("r3_debug").orElse(wrapper.getDebugPort("debug_r3")).map(wrapper.peekVal).map(_.toString).getOrElse("-")

            println(s"[Cycle $cycle]$pcStr$reqStr [Regs: R0=$r0 R1=$r1 R2=$r2 R3=$r3]")
          }

          if (wrapper.hlt.peek().litToBoolean) {
            halted = true
          }
        }

        printSummary(cycle, wrapper, mem, initialMem)
      }
    }

    // Run custom or generic simulators
    arch.toLowerCase match {
      case "ibm360" =>
        val ibmBytes = lines.map(l => Integer.parseInt(l, 16).toByte)
        val mem = Array.fill(1024)(0.toByte)
        for (i <- ibmBytes.indices) mem(i) = ibmBytes(i)

        def readWord(addr: Int): Int = {
          if (addr + 3 >= 1024) 0
          else {
            ((mem(addr) & 0xFF) << 24) |
            ((mem(addr + 1) & 0xFF) << 16) |
            ((mem(addr + 2) & 0xFF) << 8) |
            (mem(addr + 3) & 0xFF)
          }
        }

        def writeWord(addr: Int, data: Int): Unit = {
          if (addr + 3 < 1024) {
            mem(addr)     = ((data >> 24) & 0xFF).toByte
            mem(addr + 1) = ((data >> 16) & 0xFF).toByte
            mem(addr + 2) = ((data >> 8) & 0xFF).toByte
            mem(addr + 3) = (data & 0xFF).toByte
          }
        }

        simulate(new zoo.ibm360.Ibm360Core) { c =>
          val wrapper = new CoreWrapper(c)
          wrapper.mem.ready.poke(false.B)
          wrapper.mem.rdata.poke(0.U)
          c.reset.poke(true.B)
          c.clock.step(5)
          c.reset.poke(false.B)

          var cycle = 0
          var done = false
          while (cycle < maxCycles && !done) {
            val req   = wrapper.mem.req.peek().litToBoolean
            val addr  = wrapper.mem.addr.peek().litValue.toInt
            val write = wrapper.mem.write.peek().litToBoolean
            val wdata = wrapper.mem.wdata.peek().litValue.toInt

            if (req) {
              wrapper.mem.ready.poke(true.B)
              if (write) writeWord(addr, wdata)
              val rdata = readWord(addr)
              wrapper.mem.rdata.poke((rdata.toLong & 0xFFFFFFFFL).U)
            } else {
              wrapper.mem.ready.poke(false.B)
            }

            c.clock.step(1)
            cycle += 1

            val pcVal = wrapper.getDebugPort("pc_debug").map(wrapper.peekVal).getOrElse(BigInt(0)).toInt
            if (pcVal == 48) done = true

            if (trace) {
              val reqStr = if (req) s" [Mem: ${if (write) "Write" else "Read"} Addr=$addr Val=${if (write) wdata else readWord(addr)}]" else ""
              println(s"[Cycle $cycle] pc=$pcVal$reqStr")
            }
          }

          val finalMem = mem.map(b => BigInt(b & 0xFF))
          val initMem = ibmBytes.map(b => BigInt(b & 0xFF)).padTo(1024, BigInt(0))
          printSummary(cycle, wrapper, finalMem, initMem)
        }

      case "m68k" =>
        val m68kBytes = lines.map(l => Integer.parseInt(l, 16))
        val mem = scala.collection.mutable.Map[Long, Int]()
        for ((word, index) <- m68kBytes.zipWithIndex) {
          mem(index * 2L) = word
        }

        simulate(new zoo.motorola68000.M68kCore) { c =>
          val wrapper = new CoreWrapper(c)
          wrapper.mem.ready.poke(false.B)
          wrapper.mem.rdata.poke(0.U)
          c.reset.poke(true.B)
          c.clock.step(5)
          c.reset.poke(false.B)

          var cycle = 0
          var done = false
          while (cycle < maxCycles && !done) {
            val req   = wrapper.mem.req.peek().litValue > 0
            val addr  = wrapper.mem.addr.peek().litValue.toLong
            val write = wrapper.mem.write.peek().litValue > 0
            val wdata = wrapper.mem.wdata.peek().litValue.toInt

            if (req) {
              wrapper.mem.ready.poke(true.B)
              if (write) mem(addr) = wdata
              wrapper.mem.rdata.poke(mem.getOrElse(addr, 0).U(16.W))
            } else {
              wrapper.mem.ready.poke(false.B)
            }

            c.clock.step(1)
            cycle += 1

            val pcVal = wrapper.getDebugPort("debug_pc").map(wrapper.peekVal).getOrElse(BigInt(0))
            if (pcVal == 104) done = true

            if (trace) {
              val reqStr = if (req) s" [Mem: ${if (write) "Write" else "Read"} Addr=$addr Val=${if (write) wdata else mem.getOrElse(addr, 0)}]" else ""
              println(s"[Cycle $cycle] pc=$pcVal$reqStr")
            }
          }

          val finalMemArray = Array.fill(1024)(BigInt(0))
          for ((k, v) <- mem) {
            if (k >= 0 && k < 1024) finalMemArray(k.toInt) = BigInt(v)
          }
          val initMemArray = Array.fill(1024)(BigInt(0))
          for ((word, index) <- m68kBytes.zipWithIndex) {
            initMemArray(index * 2) = BigInt(word)
          }
          printSummary(cycle, wrapper, finalMemArray, initMemArray)
        }

      // Generic cases
      case "babbage" => runGeneric(new zoo.babbage.BabbageCore, 64, 256)
      case "harvard" => runGeneric(new zoo.harvardmark1.HarvardMark1Core, 64, 256)
      case "zuse" => runGeneric(new zoo.zusez1.ZuseZ1Core, 22, 256)
      case "manchestermu1" => runGeneric(new zoo.manchestermu1.Manchestermu1Core, 32, 256)
      case "univac" => runGeneric(new zoo.univac1.Univac1Core, 72, 256)
      case "princetonias" => runGeneric(new zoo.princetonias.PrincetoniasCore, 40, 256)
      case "cambridgeedsac" => runGeneric(new zoo.cambridgeedsac.CambridgeedsacCore, 17, 256)
      case "ibm701" => runGeneric(new zoo.ibm701.Ibm701Core, 36, 256)
      case "ibm704" => runGeneric(new zoo.ibm704.Ibm704Core, 36, 256)
      case "ibm650" => runGeneric(new zoo.ibm650.Ibm650Core, 40, 256)
      case "ibm705" => runGeneric(new zoo.ibm705.Ibm705Core, 35, 256)
      case "ibm1401" => runGeneric(new zoo.ibm1401.Ibm1401Core, 36, 256)
      case "stczebra" => runGeneric(new zoo.stczebra.StczebraCore, 33, 256)
      case "bullgamma" => runGeneric(new zoo.bullgamma60.Bullgamma60Core, 24, 256)
      case "ibmstretch" => runGeneric(new zoo.ibmstretch.IbmstretchCore, 64, 256)
      case "mos" => runGeneric(new zoo.mos6502.Mos6502Core, 8, 65536)
      case "pdp8" => runGeneric(new zoo.decpdp8.Pdp8Core, 12, 4096)
      case "pdp11" => runGeneric(new zoo.decpdp11.Pdp11Core, 16, 256)
      case "b5500" => runGeneric(new zoo.burroughsb5500.B5500Core, 48, 256)
      case "cdc" => runGeneric(new zoo.cdc6600.Cdc6600Core, 60, 256)
      case "cray" => runGeneric(new zoo.cray1.Cray1Core, 64, 256)
      case "univac1103a" => runGeneric(new zoo.univac1103a.Univac1103aCore, 36, 256)
      case "cdc6600ppu" => runGeneric(new zoo.cdc6600ppu.Cdc6600ppuCore, 12, 256)
      case "decvax" => runGeneric(new zoo.decvax.DecvaxCore, 32, 256)
      case "intel8080a" => runGeneric(new zoo.intel8080a.Intel8080aCore, 8, 65536)
      case "motorola6800" => runGeneric(new zoo.motorola6800.Motorola6800Core, 8, 65536)
      case "ibm6150" => runGeneric(new zoo.ibm6150.Ibm6150Core, 32, 256)
      case "mips1" => runGeneric(new zoo.mips1.Mips1Core, 32, 256)
      case "arm1" => runGeneric(new zoo.arm1.Arm1Core, 32, 256)
      case "berkeleyrisc" => runGeneric(new zoo.berkeleyrisc.BerkeleyriscCore, 32, 256)
      case "hp3000" => runGeneric(new zoo.hp3000.Hp3000Core, 16, 256)
      case "ethlilith" => runGeneric(new zoo.ethlilith.EthlilithCore, 16, 256)
      case "ucsdp" => runGeneric(new zoo.ucsdp.UcsdpCore, 16, 256)
      case "upd7720" => runGeneric(new zoo.upd7720.Upd7720Core, 16, 256)
      case "tms32010" => runGeneric(new zoo.tms32010.Tms32010Core, 16, 256)
      case "adsp2100" => runGeneric(new zoo.adsp2100.Adsp2100Core, 16, 256)
      case "ibmmwave" => runGeneric(new zoo.ibmmwave.IbmmwaveCore, 16, 256)
      case "voodoo1" => runGeneric(new zoo.voodoo1.Voodoo1Core, 32, 256)
      case "geforce256" => runGeneric(new zoo.geforce256.Geforce256Core, 32, 256)
      case "radeonr100" => runGeneric(new zoo.radeonr100.Radeonr100Core, 32, 256)
      case "powervr1" => runGeneric(new zoo.powervr1.Powervr1Core, 32, 256)
      case "mali200" => runGeneric(new zoo.mali200.Mali200Core, 32, 256)
      case "amdr600" => runGeneric(new zoo.amdr600.Amdr600Core, 32, 256)
      case other =>
        println(s"Error: unsupported core $other")
        sys.exit(1)
    }
  }

  def printSummary(cyclesRun: Int, wrapper: CoreWrapper, mem: Array[BigInt], initialMem: Array[BigInt]): Unit = {
    println(s"\n[Simulator] Simulation completed in $cyclesRun cycles.")
    println("\n=== Performance Summary ===")
    val pmuCycles = try { wrapper.pmu_cycles.peek().litValue } catch { case _: Exception => BigInt(cyclesRun) }
    val pmuInsts  = try { wrapper.pmu_insts.peek().litValue } catch { case _: Exception => BigInt(0) }
    val pmuReads  = try { wrapper.pmu_reads.peek().litValue } catch { case _: Exception => BigInt(0) }
    val pmuWrites = try { wrapper.pmu_writes.peek().litValue } catch { case _: Exception => BigInt(0) }

    println(s"* Total Cycles: $pmuCycles")
    println(s"* Retired Instructions: $pmuInsts")
    println(s"* Memory Reads: $pmuReads")
    println(s"* Memory Writes: $pmuWrites")
    val cpi = if (pmuInsts > BigInt(0)) {
      String.format("%.2f", Double.box(pmuCycles.toDouble / pmuInsts.toDouble))
    } else "N/A"
    println(s"* CPI: $cpi")

    println("\n=== Modified Memory Dump ===")
    var printed = false
    for (i <- mem.indices) {
      if (mem(i) != initialMem(i) && i < 256) {
        println(s"Addr $i: ${initialMem(i)} -> ${mem(i)} (0x${mem(i).toString(16)})")
        printed = true
      }
    }
    if (!printed) {
      println("(No modifications detected in memory ranges 0-255)")
    }
    println("============================")
  }
}
