package folio.codinginterview.application.repository

import folio.codinginterview.domain.StockSymbol
import scala.concurrent.Future

/** 市場価格リポジトリ。 */
trait MarketPriceRepository {
  def all(): Future[Map[StockSymbol, BigDecimal]]
  def update(prices: Map[StockSymbol, BigDecimal]): Future[Unit]
}
