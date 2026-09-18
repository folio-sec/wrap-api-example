package folio.codinginterview.presentation

import folio.codinginterview.application.usecase.order.AdditionalBuyOrderUsecase
import folio.codinginterview.application.usecase.order.AdditionalBuyOrderUsecaseException
import folio.codinginterview.application.usecase.order.AdditionalBuyOrderUsecaseInput
import folio.codinginterview.application.usecase.order.NewOrderUsecase
import folio.codinginterview.application.usecase.order.NewOrderUsecaseException
import folio.codinginterview.application.usecase.order.NewOrderUsecaseInput
import folio.codinginterview.presentation.PresentationException.BadRequestException
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object OrderController {
  final case class NewOrderRequest(userId: String, amount: String)
  final case class AdditionalOrderRequest(userId: String, amount: String)
}

final class OrderController(
    newOrderUsecase: NewOrderUsecase,
    additionalBuyOrderUsecase: AdditionalBuyOrderUsecase
)(using ec: ExecutionContext)
    extends PresentationPreparation {
  import OrderController.*

  def newOrder(req: NewOrderRequest): Future[Unit] =
    for {
      uid <- parseUserId(req.userId)
      amt <- parseAmount(req.amount)
      _ <- newOrderUsecase.run(NewOrderUsecaseInput(uid, amt)).recoverWith {
        case NewOrderUsecaseException.UserAlreadyExists =>
          Future.failed(BadRequestException("user already has account"))
        case NewOrderUsecaseException.AmountTooSmall =>
          Future.failed(BadRequestException("amount is too small"))
      }
    } yield ()

  def additionalOrder(req: AdditionalOrderRequest): Future[Unit] =
    for {
      uid <- parseUserId(req.userId)
      amt <- parseAmount(req.amount)
      _ <- additionalBuyOrderUsecase.run(AdditionalBuyOrderUsecaseInput(uid, amt)).recoverWith {
        case AdditionalBuyOrderUsecaseException.UserNotFound =>
          Future.failed(BadRequestException("user has no live account"))
        case AdditionalBuyOrderUsecaseException.AmountTooSmall =>
          Future.failed(BadRequestException("amount is too small"))
      }
    } yield ()
}
