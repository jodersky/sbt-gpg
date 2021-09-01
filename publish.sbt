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
    List("echo $GPG_SECRET_KEY | gpg --import"),
    name = Some("Import private key"),
    env = Map("GPG_SECRET_KEY" -> "${{ secrets.GPG_SECRET_KEY }}")),

  WorkflowStep.Run(
    List("mv sonatype.proto-sbt sonatype.sbt"),
    name = Some("Unlock publication configuration")))

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("publish", "sonatypeRelease"),
    name = Some("Release"),
    env = Map("SONATYPE_PASS" -> "${{ secrets.SONATYPE_PASS }}")))
