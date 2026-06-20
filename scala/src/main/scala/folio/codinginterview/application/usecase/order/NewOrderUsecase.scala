package folio.codinginterview.application.usecase.order

import folio.codinginterview.application.repository.AccountRepository
import folio.codinginterview.application.repository.PortfolioRepository
import folio.codinginterview.domain.Account
import folio.codinginterview.domain.AppConstants
import folio.codinginterview.domain.UserId
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

final case class NewOrderUsecaseInput(userId: UserId, amount: BigDecimal)

sealed trait NewOrderUsecaseException extends RuntimeException
object NewOrderUsecaseException {
  case object UserAlreadyExists extends NewOrderUsecaseException
  case object AmountTooSmall extends NewOrderUsecaseException
}

final class NewOrderUsecase(
    accountRepository: AccountRepository,
    portfolioRepository: PortfolioRepository
)(using ec: ExecutionContext) {
  def run(input: NewOrderUsecaseInput): Future[Unit] = {
    if (input.amount < AppConstants.minOperationAmount) {
      Future.failed(NewOrderUsecaseException.AmountTooSmall)
    } else {
      for {
        exists <- accountRepository.exists(input.userId)
        _ <-
          if (exists) Future.failed(NewOrderUsecaseException.UserAlreadyExists)
          else Future.unit
        portfolio <- portfolioRepository.get()
        account = Account.openAccount(input.amount, portfolio)
        _ <- accountRepository.upsert(input.userId, account)
      } yield ()
    }
  }
}
