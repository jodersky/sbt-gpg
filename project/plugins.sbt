addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.4.0")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.10")
addSbtPlugin("io.crashbox" % "sbt-gpg" % "0.2.0")
addSbtPlugin("com.codecommit" % "sbt-github-actions" % "0.13.0")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
