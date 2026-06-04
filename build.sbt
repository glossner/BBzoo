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

// ***************************
// * ROOT PROJECT
// ***************************
lazy val root = (project in file("."))
  .aggregate(common, motorola68000, decpdp8, ibm360, cray1, burroughsb5500, decpdp11, cdc6600, mos6502)
  .dependsOn(common, motorola68000, decpdp8, ibm360, cray1, burroughsb5500, decpdp11, cdc6600, mos6502)
  .settings(
    name := "BrooksZoo"
  )
  .settings(commonSettings: _*)
