ThisBuild / scalaVersion := "2.13.18"
ThisBuild / version      := "0.1.0"
ThisBuild / organization := "zoo"

val chiselVersion = "7.12.0"

// ***************************
// * COMMON SETTINGS
// ***************************
lazy val commonSettings = Seq(
  // Library Dependencies
  libraryDependencies ++= Seq(
    "org.chipsalliance" %% "chisel"     % chiselVersion,
    "org.scalatest"     %% "scalatest"  % "3.2.16" % Test,
    "org.slf4j"         % "slf4j-api"   % "2.0.9",
    "org.slf4j"         % "slf4j-simple"% "2.0.9"
  ),

  // Scala Compiler Options
  scalacOptions ++= Seq(
    "-language:reflectiveCalls",
    "-deprecation",
    "-feature",
    "-Xcheckinit",
    "-Ymacro-annotations"
  ),

  // Chisel Compiler Plugin
  addCompilerPlugin("org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full),

  // Fork a new JVM for running and testing
  run / fork := true,
  Test / fork := true,
  Test / parallelExecution := false
)

// ***************************
// * SUBPROJECTS
// ***************************

// 1. Shared Common Architecture Library
lazy val common = (project in file("common/rtl"))
  .settings(
    name := "ZooCommon"
  )
  .settings(commonSettings: _*)

// 2. Motorola 68000 Project
lazy val motorola68000 = (project in file("motorola68000/rtl"))
  .dependsOn(common)
  .settings(
    name := "Motorola68000"
  )
  .settings(commonSettings: _*)

// 3. DEC PDP-8 Project
lazy val decpdp8 = (project in file("decpdp8/rtl"))
  .dependsOn(common)
  .settings(
    name := "DecPdp8"
  )
  .settings(commonSettings: _*)

// 4. IBM System/360 Project
lazy val ibm360 = (project in file("ibm360/rtl"))
  .dependsOn(common)
  .settings(
    name := "Ibm360"
  )
  .settings(commonSettings: _*)

// 5. Cray-1 Project
lazy val cray1 = (project in file("cray1/rtl"))
  .dependsOn(common)
  .settings(
    name := "Cray1"
  )
  .settings(commonSettings: _*)

// 6. Burroughs B5500 Project
lazy val burroughsb5500 = (project in file("burroughsb5500/rtl"))
  .dependsOn(common)
  .settings(
    name := "BurroughsB5500"
  )
  .settings(commonSettings: _*)

// 7. DEC PDP-11 Project
lazy val decpdp11 = (project in file("decpdp11/rtl"))
  .dependsOn(common)
  .settings(
    name := "DecPdp11"
  )
  .settings(commonSettings: _*)

// 8. CDC 6600 Project
lazy val cdc6600 = (project in file("cdc6600/rtl"))
  .dependsOn(common)
  .settings(
    name := "Cdc6600"
  )
  .settings(commonSettings: _*)

// 9. MOS 6502 Project
lazy val mos6502 = (project in file("mos6502/rtl"))
  .dependsOn(common)
  .settings(
    name := "Mos6502"
  )
  .settings(commonSettings: _*)

// 10. Babbage Analytical Engine Project
lazy val babbage = (project in file("babbage/rtl"))
  .dependsOn(common)
  .settings(
    name := "Babbage"
  )
  .settings(commonSettings: _*)

// 11. Harvard Mark I Project
lazy val harvardmark1 = (project in file("harvardmark1/rtl"))
  .dependsOn(common)
  .settings(
    name := "HarvardMark1"
  )
  .settings(commonSettings: _*)

// 12. Zuse Z1 Project
lazy val zusez1 = (project in file("zusez1/rtl"))
  .dependsOn(common)
  .settings(
    name := "ZuseZ1"
  )
  .settings(commonSettings: _*)

// 13. Manchester Baby Project
lazy val manchestermu1 = (project in file("manchestermu1/rtl"))
  .dependsOn(common)
  .settings(
    name := "Manchestermu1"
  )
  .settings(commonSettings: _*)

// 14. Univac I Project
lazy val univac1 = (project in file("univac1/rtl"))
  .dependsOn(common)
  .settings(
    name := "Univac1"
  )
  .settings(commonSettings: _*)

// 15. Princeton IAS Project
lazy val princetonias = (project in file("princetonias/rtl"))
  .dependsOn(common)
  .settings(
    name := "Princetonias"
  )
  .settings(commonSettings: _*)

// 16. EDSAC Project
lazy val cambridgeedsac = (project in file("cambridgeedsac/rtl"))
  .dependsOn(common)
  .settings(
    name := "Cambridgeedsac"
  )
  .settings(commonSettings: _*)

// 17. IBM 701 Project
lazy val ibm701 = (project in file("ibm701/rtl"))
  .dependsOn(common)
  .settings(
    name := "Ibm701"
  )
  .settings(commonSettings: _*)

// 18. IBM 704 Project
lazy val ibm704 = (project in file("ibm704/rtl"))
  .dependsOn(common)
  .settings(
    name := "Ibm704"
  )
  .settings(commonSettings: _*)

// 19. IBM 650 Project
lazy val ibm650 = (project in file("ibm650/rtl"))
  .dependsOn(common)
  .settings(
    name := "Ibm650"
  )
  .settings(commonSettings: _*)

// 20. IBM 705 Project
lazy val ibm705 = (project in file("ibm705/rtl"))
  .dependsOn(common)
  .settings(
    name := "Ibm705"
  )
  .settings(commonSettings: _*)

// 21. IBM 1401 Project
lazy val ibm1401 = (project in file("ibm1401/rtl"))
  .dependsOn(common)
  .settings(
    name := "Ibm1401"
  )
  .settings(commonSettings: _*)

// 22. STC ZEBRA Project
lazy val stczebra = (project in file("stczebra/rtl"))
  .dependsOn(common)
  .settings(
    name := "Stczebra"
  )
  .settings(commonSettings: _*)

// 23. Bull Gamma 60 Project
lazy val bullgamma60 = (project in file("bullgamma60/rtl"))
  .dependsOn(common)
  .settings(
    name := "Bullgamma60"
  )
  .settings(commonSettings: _*)

// 24. IBM Stretch Project
lazy val ibmstretch = (project in file("ibmstretch/rtl"))
  .dependsOn(common)
  .settings(
    name := "Ibmstretch"
  )
  .settings(commonSettings: _*)

// 25. Univac 1103A Project
lazy val univac1103a = (project in file("univac1103a/rtl"))
  .dependsOn(common)
  .settings(
    name := "Univac1103a"
  )
  .settings(commonSettings: _*)

// 26. CDC 6600 PPU Project
lazy val cdc6600ppu = (project in file("cdc6600ppu/rtl"))
  .dependsOn(common)
  .settings(
    name := "Cdc6600ppu"
  )
  .settings(commonSettings: _*)

// 27. DEC VAX Project
lazy val decvax = (project in file("decvax/rtl"))
  .dependsOn(common)
  .settings(
    name := "DecVax"
  )
  .settings(commonSettings: _*)

// 28. Intel 8080A Project
lazy val intel8080a = (project in file("intel8080a/rtl"))
  .dependsOn(common)
  .settings(
    name := "Intel8080a"
  )
  .settings(commonSettings: _*)

// 29. Motorola 6800 Project
lazy val motorola6800 = (project in file("motorola6800/rtl"))
  .dependsOn(common)
  .settings(
    name := "Motorola6800"
  )
  .settings(commonSettings: _*)

// 30. IBM 6150 Project
lazy val ibm6150 = (project in file("ibm6150/rtl"))
  .dependsOn(common)
  .settings(
    name := "Ibm6150"
  )
  .settings(commonSettings: _*)

// 31. MIPS I Project
lazy val mips1 = (project in file("mips1/rtl"))
  .dependsOn(common)
  .settings(
    name := "Mips1"
  )
  .settings(commonSettings: _*)

// 32. ARM1 Project
lazy val arm1 = (project in file("arm1/rtl"))
  .dependsOn(common)
  .settings(
    name := "Arm1"
  )
  .settings(commonSettings: _*)

// 33. Berkeley RISC-I Project
lazy val berkeleyrisc = (project in file("berkeleyrisc/rtl"))
  .dependsOn(common)
  .settings(
    name := "Berkeleyrisc"
  )
  .settings(commonSettings: _*)

// 34. HP 3000 Project
lazy val hp3000 = (project in file("hp3000/rtl"))
  .dependsOn(common)
  .settings(
    name := "Hp3000"
  )
  .settings(commonSettings: _*)

// 35. Ethlilith Project
lazy val ethlilith = (project in file("ethlilith/rtl"))
  .dependsOn(common)
  .settings(
    name := "Ethlilith"
  )
  .settings(commonSettings: _*)

// 36. UCSD Pascal P-Machine Project
lazy val ucsdp = (project in file("ucsdp/rtl"))
  .dependsOn(common)
  .settings(
    name := "Ucsdp"
  )
  .settings(commonSettings: _*)

// 37. NEC uPD7720 DSP Project
lazy val upd7720 = (project in file("upd7720/rtl"))
  .dependsOn(common)
  .settings(
    name := "Upd7720"
  )
  .settings(commonSettings: _*)

// 38. TI TMS32010 DSP Project
lazy val tms32010 = (project in file("tms32010/rtl"))
  .dependsOn(common)
  .settings(
    name := "Tms32010"
  )
  .settings(commonSettings: _*)

// 39. ADI ADSP-2100 DSP Project
lazy val adsp2100 = (project in file("adsp2100/rtl"))
  .dependsOn(common)
  .settings(
    name := "Adsp2100"
  )
  .settings(commonSettings: _*)

// 40. IBM MWave DSP Project
lazy val ibmmwave = (project in file("ibmmwave/rtl"))
  .dependsOn(common)
  .settings(
    name := "Ibmmwave"
  )
  .settings(commonSettings: _*)

// 41. 3dfx Voodoo1 Project
lazy val voodoo1 = (project in file("voodoo1/rtl"))
  .dependsOn(common)
  .settings(
    name := "Voodoo1"
  )
  .settings(commonSettings: _*)

// 42. NVIDIA GeForce 256 Project
lazy val geforce256 = (project in file("geforce256/rtl"))
  .dependsOn(common)
  .settings(
    name := "Geforce256"
  )
  .settings(commonSettings: _*)

// 43. ATI Radeon R100 Project
lazy val radeonr100 = (project in file("radeonr100/rtl"))
  .dependsOn(common)
  .settings(
    name := "Radeonr100"
  )
  .settings(commonSettings: _*)

// 44. PowerVR Series 1 Project
lazy val powervr1 = (project in file("powervr1/rtl"))
  .dependsOn(common)
  .settings(
    name := "Powervr1"
  )
  .settings(commonSettings: _*)

// 45. ARM Mali-200 Project
lazy val mali200 = (project in file("mali200/rtl"))
  .dependsOn(common)
  .settings(
    name := "Mali200"
  )
  .settings(commonSettings: _*)

// 46. AMD R600 Project
lazy val amdr600 = (project in file("amdr600/rtl"))
  .dependsOn(common)
  .settings(
    name := "Amdr600"
  )
  .settings(commonSettings: _*)

// 47. AMD Am2901 Project
lazy val amd2901 = (project in file("amd2901/rtl"))
  .dependsOn(common)
  .settings(
    name := "Amd2901"
  )
  .settings(commonSettings: _*)

// 48. Intel 3002 Project
lazy val intel3002 = (project in file("intel3002/rtl"))
  .dependsOn(common)
  .settings(
    name := "Intel3002"
  )
  .settings(commonSettings: _*)

// 49. National Semiconductor IMP-16 Project
lazy val imp16 = (project in file("imp16/rtl"))
  .dependsOn(common)
  .settings(
    name := "Imp16"
  )
  .settings(commonSettings: _*)

// 50. Motorola MC10800 Project
lazy val mc10800 = (project in file("mc10800/rtl"))
  .dependsOn(common)
  .settings(
    name := "Mc10800"
  )
  .settings(commonSettings: _*)

// 51. ILLIAC IV Project
lazy val illiac4 = (project in file("illiac4/rtl"))
  .dependsOn(common)
  .settings(
    name := "Illiac4"
  )
  .settings(commonSettings: _*)

// 52. ICL DAP Project
lazy val icldap = (project in file("icldap/rtl"))
  .dependsOn(common)
  .settings(
    name := "Icldap"
  )
  .settings(commonSettings: _*)

// 53. Goodyear MPP Project
lazy val goodmpp = (project in file("goodmpp/rtl"))
  .dependsOn(common)
  .settings(
    name := "Goodmpp"
  )
  .settings(commonSettings: _*)

// 54. Thinking Machines Connection Machine CM-1 Project
lazy val cm1 = (project in file("cm1/rtl"))
  .dependsOn(common)
  .settings(
    name := "Cm1"
  )
  .settings(commonSettings: _*)

// 55. IBM MFAST Project
lazy val ibmmfast = (project in file("ibmmfast/rtl"))
  .dependsOn(common)
  .settings(
    name := "Ibmmfast"
  )
  .settings(commonSettings: _*)

// 56. Multiflow TRACE Project
lazy val multiflow = (project in file("multiflow/rtl"))
  .dependsOn(common)
  .settings(
    name := "Multiflow"
  )
  .settings(commonSettings: _*)

// 57. Cydrome Cydra 5 Project
lazy val cydra5 = (project in file("cydra5/rtl"))
  .dependsOn(common)
  .settings(
    name := "Cydra5"
  )
  .settings(commonSettings: _*)

// 58. TI TMS320C6000 Project
lazy val tms320c6k = (project in file("tms320c6k/rtl"))
  .dependsOn(common)
  .settings(
    name := "Tms320c6k"
  )
  .settings(commonSettings: _*)

// 59. Transmeta Crusoe Project
lazy val crusoe = (project in file("crusoe/rtl"))
  .dependsOn(common)
  .settings(
    name := "Crusoe"
  )
  .settings(commonSettings: _*)

// 60. Intel Itanium Project
lazy val itanium = (project in file("itanium/rtl"))
  .dependsOn(common)
  .settings(
    name := "Itanium"
  )
  .settings(commonSettings: _*)

// ***************************
// * ROOT PROJECT
// ***************************
lazy val root = (project in file("."))
  .aggregate(common, motorola68000, decpdp8, ibm360, cray1, burroughsb5500, decpdp11, cdc6600, mos6502, babbage, harvardmark1, zusez1, manchestermu1, univac1, princetonias, cambridgeedsac, ibm701, ibm704, ibm650, ibm705, ibm1401, stczebra, bullgamma60, ibmstretch, univac1103a, cdc6600ppu, decvax, intel8080a, motorola6800, ibm6150, mips1, arm1, berkeleyrisc, hp3000, ethlilith, ucsdp, upd7720, tms32010, adsp2100, ibmmwave, voodoo1, geforce256, radeonr100, powervr1, mali200, amdr600, amd2901, intel3002, imp16, mc10800, illiac4, icldap, goodmpp, cm1, ibmmfast, multiflow, cydra5, tms320c6k, crusoe, itanium)
  .dependsOn(common, motorola68000, decpdp8, ibm360, cray1, burroughsb5500, decpdp11, cdc6600, mos6502, babbage, harvardmark1, zusez1, manchestermu1, univac1, princetonias, cambridgeedsac, ibm701, ibm704, ibm650, ibm705, ibm1401, stczebra, bullgamma60, ibmstretch, univac1103a, cdc6600ppu, decvax, intel8080a, motorola6800, ibm6150, mips1, arm1, berkeleyrisc, hp3000, ethlilith, ucsdp, upd7720, tms32010, adsp2100, ibmmwave, voodoo1, geforce256, radeonr100, powervr1, mali200, amdr600, amd2901, intel3002, imp16, mc10800, illiac4, icldap, goodmpp, cm1, ibmmfast, multiflow, cydra5, tms320c6k, crusoe, itanium)
  .settings(
    name := "BBZoo"
  )
  .settings(commonSettings: _*)




