enablePlugins(SbtPlugin)
name := "sbt-gpg"
ThisBuild / version := {
  import sys.process._
  ("git describe --always --dirty=-SNAPSHOT --match v[0-9].*" !!).tail.trim
}

ThisBuild / scalaVersion := "2.12.14"

ThisBuild / githubWorkflowBuildPreamble += WorkflowStep.Sbt(List("scalafmtCheck"))

ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(List("test", "scripted")))

scalacOptions += "-target:jvm-1.8"

scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
}
scriptedBufferLog := false
