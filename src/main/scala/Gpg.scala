package io.crashbox.gpg

import java.io.File

import scala.util.control.NonFatal
import sys.process._

class Gpg(
    command: String,
    options: Seq[String] = Seq.empty,
    keyId: Option[String] = None)(log: String => Unit = System.err.println) {

  def run(params: String*): Int =
    try {
      val idOption = keyId.toSeq.flatMap(id => Seq("--local-user", id))
      val process = Process(command, options ++ idOption ++ params).run()
      process.exitValue()
    } catch {
      case NonFatal(ex) =>
        log(ex.getMessage)
        127
    }

  def sign(file: File): Option[File] = {
    val out = new File(file.getAbsolutePath + ".asc")
    run("--armor",
        "--output",
        out.getAbsolutePath,
        "--detach-sign",
        file.getAbsolutePath) match {
      case 0 => Some(out)
      case _ => None
    }
  }

}
