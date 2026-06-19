package folio.codinginterview.application.usecase.order

import folio.codinginterview.application.repository.AccountRepository
import folio.codinginterview.application.repository.PortfolioRepository
import folio.codinginterview.domain.UserId
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

final case class RebalanceOrderUsecaseInput(userId: UserId)

sealed trait RebalanceOrderUsecaseException extends RuntimeException
object RebalanceOrderUsecaseException {
  case object UserNotFound extends RebalanceOrderUsecaseException
}

final class RebalanceOrderUsecase(
    accountRepository: AccountRepository,
    portfolioRepository: PortfolioRepository
)(using ec: ExecutionContext) {
  def run(input: RebalanceOrderUsecaseInput): Future[Unit] = {
    for {
      maybeAccount <- accountRepository.find(input.userId)
      account <- maybeAccount match {
        case Some(a) => Future.successful(a)
        case None    => Future.failed(RebalanceOrderUsecaseException.UserNotFound)
      }
      portfolio <- portfolioRepository.get()
      updated = account.rebalance(portfolio)
      _ <- accountRepository.upsert(input.userId, updated)
    } yield ()
  }
}
