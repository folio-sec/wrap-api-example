package folio.codinginterview.presentation

import folio.codinginterview.application.usecase.market_price.UpdateMarketPriceItemInput
import folio.codinginterview.application.usecase.market_price.UpdateMarketPriceUsecase
import folio.codinginterview.application.usecase.market_price.UpdateMarketPriceUsecaseInput
import folio.codinginterview.domain.StockSymbol
import folio.codinginterview.presentation.PresentationException.BadRequestException
import scala.concurrent.Future

object MarketPriceController {
  final case class MarketPriceItemDto(symbol: String, market_price: String)
  final case class UpdateMarketPriceRequest(market_prices: Seq[MarketPriceItemDto])
}

final class MarketPriceController(
    updateMarketPriceUsecase: UpdateMarketPriceUsecase
) {
  import MarketPriceController.*

  def updateMarketPrice(req: UpdateMarketPriceRequest): Future[Unit] = {
    val parsed: Either[String, Seq[UpdateMarketPriceItemInput]] =
      req.market_prices.foldLeft[Either[String, Seq[UpdateMarketPriceItemInput]]](Right(Seq.empty)) { case (acc, dto) =>
        for {
          xs <- acc
          sym <- StockSymbol
            .fromString(dto.symbol)
            .toRight(s"unknown symbol: ${dto.symbol}")
          price <-
            try Right(BigDecimal(dto.market_price))
            catch { case _: Throwable => Left(s"invalid market_price: ${dto.market_price}") }
        } yield xs :+ UpdateMarketPriceItemInput(sym, price)
      }
    parsed match {
      case Left(msg)    => Future.failed(BadRequestException(msg))
      case Right(items) => updateMarketPriceUsecase.run(UpdateMarketPriceUsecaseInput(items))
    }
  }
}
