package folio.codinginterview.presentation

import folio.codinginterview.domain.UserId
import folio.codinginterview.presentation.PresentationException.BadRequestException
import scala.concurrent.Future
import scala.util.control.NonFatal

trait PresentationPreparation {
  protected def parseUserId(s: String): Future[UserId] =
    try Future.successful(UserId(s))
    catch { case NonFatal(e) => Future.failed(BadRequestException(e.getMessage)) }

  protected def parseAmount(s: String): Future[BigDecimal] =
    try Future.successful(BigDecimal(s))
    catch { case NonFatal(_) => Future.failed(BadRequestException(s"invalid amount: $s")) }
}
