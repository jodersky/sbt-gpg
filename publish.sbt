ThisBuild / organization := "io.crashbox"

ThisBuild / licenses := Seq(
  ("Apache 2.0", url("https://www.apache.org/licenses/LICENSE-2.0")))

ThisBuild / homepage := Some(url("https://github.com/jodersky/sbt-gpg"))

ThisBuild / publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/jodersky/sbt-gpg"),
    "scm:git@github.com:jodersky/sbt-gpg.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "jodersky",
    name = "Jakob Odersky",
    email = "jakob@odersky.com",
    url = url("https://crashbox.io")
  )
)

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")

ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(RefPredicate.StartsWith(Ref.Tag("v")))

ThisBuild / githubWorkflowPublishPreamble ++= Seq(
  WorkflowStep.Run(
    List("gpg --keyserver keyserver.ubuntu.com --recv-keys DC6A9A5E884B2D680E080467E107A4A6CF561C67"),
    name = Some("Download public key")),

  WorkflowStep.Run(
    List("openssl aes-256-cbc -K \"$PRIVATE_KEY_PASSWORD\" -iv \"$PRIVATE_KEY_IV\" -in .ci/sec.gpg.enc -out sec.gpg -d"),
    name = Some("Decrypt private key"),
    env = Map(
      "PRIVATE_KEY_PASSWORD" -> "${{ secrets.PRIVATE_KEY_PASSWORD }}",
      "PRIVATE_KEY_IV" -> "${{ secrets.PRIVATE_KEY_IV }}")),

  WorkflowStep.Run(
    List("mv sonatype.proto-sbt sonatype.sbt"),
    name = Some("Unlock publication configuration")))

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(List("publish", "sonatypeRelease"), name = Some("Release")))
