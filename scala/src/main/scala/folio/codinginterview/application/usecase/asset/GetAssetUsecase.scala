package folio.codinginterview.application.usecase.asset

import folio.codinginterview.application.repository.AccountRepository
import folio.codinginterview.domain.StockSymbol
import folio.codinginterview.domain.UserId
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

final case class GetAssetUsecaseInput(userId: UserId)

final case class GetAssetStockOutput(symbol: StockSymbol, amountJpy: BigDecimal)

final case class GetAssetUsecaseOutput(
    cashAmount: BigDecimal,
    stocks: Seq[GetAssetStockOutput]
)

sealed trait GetAssetUsecaseException extends RuntimeException
object GetAssetUsecaseException {
  case object UserNotFound extends GetAssetUsecaseException
}

final class GetAssetUsecase(
    accountRepository: AccountRepository
)(using ec: ExecutionContext) {
  def run(input: GetAssetUsecaseInput): Future[GetAssetUsecaseOutput] = {
    for {
      maybeAccount <- accountRepository.find(input.userId)
      account <- maybeAccount match {
        case Some(a) => Future.successful(a)
        case None    => Future.failed(GetAssetUsecaseException.UserNotFound)
      }
    } yield {
      val stocks = account.stocks.map { e =>
        GetAssetStockOutput(e.symbol, e.amountJpy)
      }
      GetAssetUsecaseOutput(account.cash, stocks)
    }
  }
}
