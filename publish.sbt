organization in ThisBuild := "io.crashbox"
licenses in ThisBuild := Seq(
  ("Apache 2.0", url("https://www.apache.org/licenses/LICENSE-2.0")))
homepage in ThisBuild := Some(url("https://github.com/jodersky/sbt-gpg"))
publishMavenStyle in ThisBuild := true
publishTo in ThisBuild := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)
scmInfo in ThisBuild := Some(
  ScmInfo(
    url("https://github.com/jodersky/sbt-gpg"),
    "scm:git@github.com:jodersky/sbt-gpg.git"
  )
)
developers in ThisBuild := List(
  Developer(
    id = "jodersky",
    name = "Jakob Odersky",
    email = "jakob@odersky.com",
    url = url("https://crashbox.io")
  )
)
