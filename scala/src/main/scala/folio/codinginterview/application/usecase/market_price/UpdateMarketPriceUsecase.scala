package folio.codinginterview.application.usecase.market_price

import folio.codinginterview.application.repository.MarketPriceRepository
import folio.codinginterview.domain.StockSymbol
import scala.concurrent.Future

final case class UpdateMarketPriceItemInput(symbol: StockSymbol, marketPrice: BigDecimal)
final case class UpdateMarketPriceUsecaseInput(items: Seq[UpdateMarketPriceItemInput])

final class UpdateMarketPriceUsecase(
    marketPriceRepository: MarketPriceRepository
) {
  def run(input: UpdateMarketPriceUsecaseInput): Future[Unit] = {
    val prices = input.items.map(i => i.symbol -> i.marketPrice).toMap
    marketPriceRepository.update(prices)
  }
}
