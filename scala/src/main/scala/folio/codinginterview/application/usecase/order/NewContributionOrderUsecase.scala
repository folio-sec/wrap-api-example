package folio.codinginterview.application.usecase.order

import folio.codinginterview.application.repository.AccountRepository
import folio.codinginterview.application.repository.MarketPriceRepository
import folio.codinginterview.application.repository.PortfolioRepository
import folio.codinginterview.application.service.PortfolioService
import folio.codinginterview.domain.AppConstants
import folio.codinginterview.domain.UserId
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

final case class NewContributionOrderUsecaseInput(userId: UserId, amount: BigDecimal)

sealed trait NewContributionOrderUsecaseException extends RuntimeException
object NewContributionOrderUsecaseException {
  case object UserAlreadyExists extends NewContributionOrderUsecaseException
  case object AmountTooSmall extends NewContributionOrderUsecaseException
}

final class NewContributionOrderUsecase(
    accountRepository: AccountRepository,
    portfolioRepository: PortfolioRepository,
    marketPriceRepository: MarketPriceRepository
)(using ec: ExecutionContext) {
  def run(input: NewContributionOrderUsecaseInput): Future[Unit] = {
    if (input.amount < AppConstants.minOperationAmount) {
      Future.failed(NewContributionOrderUsecaseException.AmountTooSmall)
    } else {
      for {
        exists <- accountRepository.exists(input.userId)
        _ <-
          if (exists) Future.failed(NewContributionOrderUsecaseException.UserAlreadyExists)
          else Future.unit
        portfolio <- portfolioRepository.get()
        prices <- marketPriceRepository.all()
        account = PortfolioService.allocateNew(input.amount, portfolio, prices)
        _ <- accountRepository.upsert(input.userId, account)
      } yield ()
    }
  }
}
