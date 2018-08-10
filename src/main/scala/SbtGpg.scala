package io.crashbox.gpg

import sbt.{AutoPlugin, Def, _}
import sbt.Keys._
import sbt.plugins.JvmPlugin

object SbtGpg extends AutoPlugin {

  override def requires = JvmPlugin
  override def trigger = allRequirements

  object autoImport {

    val gpgWarnOnFailure = settingKey[Boolean](
      "If true, only issue a warning when signing fails. If false, error " +
        "and fail the build. Defaults to true in publishLocal, false in publish.")

    val gpgCommand = settingKey[String]("Path to GnuPG executable.")

    val gpgOptions =
      settingKey[Seq[String]]("Additional global options to pass to gpg.")

    val gpgKey = taskKey[Option[String]](
      "Key ID used to sign artifacts. Setting this to None will " +
        "cause sbt-gpg to fall back to using gpg's default key. When set, " +
        "it is equivalent to gpg's `--local-user` option.")

    val gpg =
      taskKey[Gpg]("Utility wrapper to the underlying gpg executable.")
  }

  def packagedArtifactsImpl(
      arts: Map[Artifact, File],
      gpg: Gpg,
      warnOnFailure: Boolean)(warn: String => Unit): Map[Artifact, File] = {

    val (signatures, failure) = arts.foldLeft((Map[Artifact, File](), false)) {
      case ((acc, false), (art, file)) =>
        gpg.sign(file) match {
          case Some(signed) =>
            (acc + (art.withExtension(art.extension + ".asc") -> signed), false)

          case None =>
            val report: String => Unit =
              if (warnOnFailure) warn else sys.error(_)

            report("GPG reported an error. Artifacts won't be signed.")
            (acc, true)
        }

      case (pair @ (_, true), _) => pair
    }

    // if we fail the signing part-way through, we throw out *all* the signatures
    if (failure) arts else signatures ++ arts
  }

  import autoImport._

  lazy val gpgSettings: Seq[Setting[_]] = Seq(
    gpgWarnOnFailure := false,
    publishLocal / gpgWarnOnFailure := true,
    gpgCommand := "gpg",
    gpgOptions := Seq("--yes"),
    gpgKey := Credentials.forHost(credentials.value, "gpg").map(_.userName),
    gpg := {
      val log = streams.value.log
      new Gpg(gpgCommand.value, gpgOptions.value, gpgKey.value)(log.warn(_))
    }
  )

  lazy val signingSettings: Seq[Setting[_]] = Seq(
    publish / packagedArtifacts := {
      packagedArtifactsImpl(
        (publish / packagedArtifacts).value,
        gpg.value,
        (publish / gpgWarnOnFailure).value)(streams.value.log.warn(_))
    },
    publishLocal / packagedArtifacts := {
      packagedArtifactsImpl(
        (publishLocal / packagedArtifacts).value,
        gpg.value,
        (publishLocal / gpgWarnOnFailure).value)(streams.value.log.warn(_))

    }
  )

  override lazy val projectSettings
    : Seq[Def.Setting[_]] = gpgSettings ++ signingSettings

}
