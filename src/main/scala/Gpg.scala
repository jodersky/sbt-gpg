package io.crashbox.gpg

import java.io.File
import java.util.concurrent.Semaphore

import scala.util.control.NonFatal
import sys.process._

class Gpg(command: String,
          options: Seq[String] = Seq.empty,
          keyId: Option[String] = None)(
    info: String => Unit = System.out.println,
    warn: String => Unit = System.err.println) {

  private val logger = ProcessLogger(info, info) // gpg uses stderr for everything; redirect to info

  def run(params: String*): Int =
    try {
      val idOption = keyId.toSeq.flatMap(id => Seq("--local-user", id))
      val process = Process(command, options ++ idOption ++ params).run(logger)
      process.exitValue()
    } catch {
      case NonFatal(ex) =>
        warn(ex.getMessage)
        127
    }

  def sign(file: File): Option[File] = {
    Gpg.gate.acquire()
    try {
      val out = new File(file.getAbsolutePath + ".asc")
      run("--armor",
          "--output",
          out.getAbsolutePath,
          "--detach-sign",
          file.getAbsolutePath) match {
        case 0 => Some(out)
        case _ => None
      }
    } finally {
      Gpg.gate.release()
    }
  }

}

object Gpg {
  private val gate = new Semaphore(1) // TODO tune this better (and maybe make it configurable by settings)
}
