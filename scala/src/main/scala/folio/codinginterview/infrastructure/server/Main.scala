package folio.codinginterview.infrastructure.server

import scala.concurrent.ExecutionContext

object Main {
  def main(args: Array[String]): Unit = {
    given ExecutionContext = ExecutionContext.global
    val _ = DummyServer.default()
    println("DummyServer initialized.")
  }
}
