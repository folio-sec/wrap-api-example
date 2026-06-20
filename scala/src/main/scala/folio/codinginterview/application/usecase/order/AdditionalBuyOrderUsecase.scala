package folio.codinginterview.application.usecase.order

import folio.codinginterview.application.repository.AccountRepository
import folio.codinginterview.application.repository.PortfolioRepository
import folio.codinginterview.domain.AppConstants
import folio.codinginterview.domain.UserId
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

final case class AdditionalBuyOrderUsecaseInput(userId: UserId, amount: BigDecimal)

sealed trait AdditionalBuyOrderUsecaseException extends RuntimeException
object AdditionalBuyOrderUsecaseException {
  case object UserNotFound extends AdditionalBuyOrderUsecaseException
  case object AmountTooSmall extends AdditionalBuyOrderUsecaseException
}

final class AdditionalBuyOrderUsecase(
    accountRepository: AccountRepository,
    portfolioRepository: PortfolioRepository
)(using ec: ExecutionContext) {
  def run(input: AdditionalBuyOrderUsecaseInput): Future[Unit] = {
    if (input.amount < AppConstants.minOperationAmount) {
      Future.failed(AdditionalBuyOrderUsecaseException.AmountTooSmall)
    } else {
      for {
        maybeAccount <- accountRepository.find(input.userId)
        account <- maybeAccount match {
          case Some(a) => Future.successful(a)
          case None    => Future.failed(AdditionalBuyOrderUsecaseException.UserNotFound)
        }
        portfolio <- portfolioRepository.get()
        updated = account.addFunds(input.amount, portfolio)
        _ <- accountRepository.upsert(input.userId, updated)
      } yield ()
    }
  }
}
