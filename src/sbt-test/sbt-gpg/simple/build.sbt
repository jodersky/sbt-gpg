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
        "AF19 CAC5 0D55 E6AE E0D1  F28E F05C 07EE CC58 F7C3",
        "ignored"
      )
    }
  )
  .settings(
    TaskKey[Unit]("check") := {
      val artifacts: Map[Artifact, java.io.File] =
        packagedArtifacts.value

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
