package folio.codinginterview.infrastructure.repository

import folio.codinginterview.application.repository.MarketPriceRepository
import folio.codinginterview.domain.AppConstants
import folio.codinginterview.domain.StockSymbol
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.Future

final class MarketPriceRepositoryImpl extends MarketPriceRepository {
  private val ref: AtomicReference[Map[StockSymbol, BigDecimal]] =
    new AtomicReference(AppConstants.initialPrices)

  override def all(): Future[Map[StockSymbol, BigDecimal]] = Future.successful(ref.get())

  override def update(prices: Map[StockSymbol, BigDecimal]): Future[Unit] = {
    ref.set(prices)
    Future.unit
  }
}
