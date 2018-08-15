import sbt.librarymanagement.Artifact
import sys.process._

lazy val root = project
  .in(file("."))
  .settings(
    credentials += {
      "gpg --import snakeoil.asc".!!
      Credentials(
        "GnuPG Key ID",
        "gpg",
        "764D 70D5 5506 DB6F 0D3C  F3D3 3665 C9FC E716 EC52",
        "ignored"
      )
    }
  )
  .settings(
    TaskKey[Unit]("check") := {
      val artifacts: Map[Artifact, java.io.File] =
        (publish / packagedArtifacts).value

      // check that every artifact is signed and that the actual signature file
      // exists
      artifacts.foreach{ case (art, file) =>
        if (art.extension.endsWith(".asc")) {
          file.exists || sys.error(s"Signature file $file does not exist")
        } else {
          artifacts.contains(art.withExtension(art.extension + ".asc")) ||
            sys.error(s"Found unsigned artifact: $art ($file)")
        }
      }
      ()
    }
  )
