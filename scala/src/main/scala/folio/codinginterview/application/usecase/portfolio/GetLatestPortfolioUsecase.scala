package folio.codinginterview.application.usecase.portfolio

import folio.codinginterview.application.repository.PortfolioRepository
import folio.codinginterview.domain.StockSymbol
import folio.codinginterview.domain.Portfolio
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

case object GetLatestPortfolioUsecaseInput

final case class GetLatestPortfolioItemOutput(symbol: StockSymbol, rate: BigDecimal)

final case class GetLatestPortfolioUsecaseOutput(items: Seq[GetLatestPortfolioItemOutput])

sealed trait GetLatestPortfolioUsecaseException extends RuntimeException

final class GetLatestPortfolioUsecase(
    portfolioRepository: PortfolioRepository
)(using ec: ExecutionContext) {
  def run(): Future[GetLatestPortfolioUsecaseOutput] = {
    portfolioRepository.get().map { p =>
      GetLatestPortfolioUsecaseOutput(
        p.items.map(i => GetLatestPortfolioItemOutput(i.symbol, i.rate))
      )
    }
  }
}
