package io.crashbox.gpg

import sbt.{AutoPlugin, Def, _}
import sbt.Keys._
import sbt.plugins.JvmPlugin

object SbtGpg extends AutoPlugin {

  override def requires = JvmPlugin
  override def trigger = allRequirements

  object autoImport {
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
  import autoImport._

  lazy val gpgSettings: Seq[Setting[_]] = Seq(
    gpgCommand := "gpg",
    gpgOptions := Seq("--yes"),
    gpgKey := Credentials.forHost(credentials.value, "gpg").map(_.userName),
    gpg := {
      val log = streams.value.log
      new Gpg(gpgCommand.value, gpgOptions.value, gpgKey.value)(log.warn(_))
    }
  )

  lazy val signingSettings: Seq[Setting[_]] = Seq(
    packagedArtifacts := {
      val log = streams.value.log
      val arts: Map[Artifact, File] = packagedArtifacts.value
      var failed = false
      arts.map {
        case (art, file) if !failed =>
          gpg.value.sign(file) match {
            case Some(signed) =>
              art.withExtension(art.extension + ".asc") -> signed
            case None =>
              log.warn("GPG reported an error. Artifacts won't be signed.")
              failed = true
              art -> file
          }
        case (art, file) => art -> file
      }
    }
  )

  override lazy val projectSettings
    : Seq[Def.Setting[_]] = gpgSettings ++ signingSettings

}
