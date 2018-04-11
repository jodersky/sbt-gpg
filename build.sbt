sbtPlugin := true
name := "sbt-gpg"
version in ThisBuild := {
  import sys.process._
  ("git describe --always --dirty=-SNAPSHOT --match v[0-9].*" !!).tail.trim
}
