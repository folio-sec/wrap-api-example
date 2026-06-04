package folio.codinginterview.presentation

import folio.codinginterview.application.usecase.order.AdditionalBuyOrderUsecase
import folio.codinginterview.application.usecase.order.AdditionalBuyOrderUsecaseException
import folio.codinginterview.application.usecase.order.AdditionalBuyOrderUsecaseInput
import folio.codinginterview.application.usecase.order.NewContributionOrderUsecase
import folio.codinginterview.application.usecase.order.NewContributionOrderUsecaseException
import folio.codinginterview.application.usecase.order.NewContributionOrderUsecaseInput
import folio.codinginterview.application.usecase.order.RebalanceOrderUsecase
import folio.codinginterview.application.usecase.order.RebalanceOrderUsecaseException
import folio.codinginterview.application.usecase.order.RebalanceOrderUsecaseInput
import folio.codinginterview.presentation.PresentationException.BadRequestException
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object OrderController {
  final case class NewContributionOrderRequest(userId: String, amount: String)
  final case class AdditionalContributionOrderRequest(userId: String, amount: String)
  final case class RebalanceOrderRequest(userId: String)
}

final class OrderController(
    newContributionOrderUsecase: NewContributionOrderUsecase,
    additionalBuyOrderUsecase: AdditionalBuyOrderUsecase,
    rebalanceOrderUsecase: RebalanceOrderUsecase
)(using ec: ExecutionContext)
    extends PresentationPreparation {
  import OrderController.*

  def newContributionOrder(req: NewContributionOrderRequest): Future[Unit] =
    for {
      uid <- parseUserId(req.userId)
      amt <- parseAmount(req.amount)
      _ <- newContributionOrderUsecase.run(NewContributionOrderUsecaseInput(uid, amt)).recoverWith {
        case NewContributionOrderUsecaseException.UserAlreadyExists =>
          Future.failed(BadRequestException("user already has account"))
        case NewContributionOrderUsecaseException.AmountTooSmall =>
          Future.failed(BadRequestException("amount is too small"))
      }
    } yield ()

  def additionalContributionOrder(req: AdditionalContributionOrderRequest): Future[Unit] =
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

  def rebalanceOrder(req: RebalanceOrderRequest): Future[Unit] =
    for {
      uid <- parseUserId(req.userId)
      _ <- rebalanceOrderUsecase.run(RebalanceOrderUsecaseInput(uid)).recoverWith {
        case RebalanceOrderUsecaseException.UserNotFound =>
          Future.failed(BadRequestException("user has no live account"))
      }
    } yield ()
}
