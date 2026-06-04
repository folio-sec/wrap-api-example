package folio.codinginterview.presentation

sealed trait PresentationException extends RuntimeException

object PresentationException {
  final case class BadRequestException(message: String) extends PresentationException {
    override def getMessage: String = message
  }
}
