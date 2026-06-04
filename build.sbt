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
lazy val manchester = (project in file("manchester/rtl"))
  .dependsOn(common)
  .settings(
    name := "Manchester"
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
lazy val ias = (project in file("ias/rtl"))
  .dependsOn(common)
  .settings(
    name := "Ias"
  )
  .settings(commonSettings: _*)

// 16. EDSAC Project
lazy val edsac = (project in file("edsac/rtl"))
  .dependsOn(common)
  .settings(
    name := "Edsac"
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

// ***************************
// * ROOT PROJECT
// ***************************
lazy val root = (project in file("."))
  .aggregate(common, motorola68000, decpdp8, ibm360, cray1, burroughsb5500, decpdp11, cdc6600, mos6502, babbage, harvardmark1, zusez1, manchester, univac1, ias, edsac, ibm701, ibm704, ibm650, ibm705, ibm1401, stczebra, bullgamma60, ibmstretch, univac1103a, cdc6600ppu)
  .dependsOn(common, motorola68000, decpdp8, ibm360, cray1, burroughsb5500, decpdp11, cdc6600, mos6502, babbage, harvardmark1, zusez1, manchester, univac1, ias, edsac, ibm701, ibm704, ibm650, ibm705, ibm1401, stczebra, bullgamma60, ibmstretch, univac1103a, cdc6600ppu)
  .settings(
    name := "BrooksZoo"
  )
  .settings(commonSettings: _*)



