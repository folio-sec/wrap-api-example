package folio.codinginterview.application.usecase.portfolio

import folio.codinginterview.application.repository.PortfolioRepository
import folio.codinginterview.domain.StockSymbol
import folio.codinginterview.domain.Portfolio
import folio.codinginterview.domain.PortfolioItem
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.control.NonFatal

final case class UpdatePortfolioItemInput(symbol: StockSymbol, rate: BigDecimal)
final case class UpdatePortfolioUsecaseInput(items: Seq[UpdatePortfolioItemInput])

sealed trait UpdatePortfolioUsecaseException extends RuntimeException
object UpdatePortfolioUsecaseException {
  final case class InvalidPortfolio(reason: String) extends UpdatePortfolioUsecaseException
}

final class UpdatePortfolioUsecase(
    portfolioRepository: PortfolioRepository
)(using ec: ExecutionContext) {
  def run(input: UpdatePortfolioUsecaseInput): Future[Unit] = {
    Future {
      Portfolio(input.items.map(i => PortfolioItem(i.symbol, i.rate)))
    }.recoverWith { case NonFatal(e) =>
      Future.failed(UpdatePortfolioUsecaseException.InvalidPortfolio(e.getMessage))
    }.flatMap(p => portfolioRepository.update(p))
  }
}
