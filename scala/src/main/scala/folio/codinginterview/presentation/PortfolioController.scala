package folio.codinginterview.presentation

import folio.codinginterview.application.usecase.portfolio.GetLatestPortfolioUsecase
import folio.codinginterview.application.usecase.portfolio.UpdatePortfolioItemInput
import folio.codinginterview.application.usecase.portfolio.UpdatePortfolioUsecase
import folio.codinginterview.application.usecase.portfolio.UpdatePortfolioUsecaseException
import folio.codinginterview.application.usecase.portfolio.UpdatePortfolioUsecaseInput
import folio.codinginterview.domain.StockSymbol
import folio.codinginterview.presentation.PresentationException.BadRequestException
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object PortfolioController {
  final case class PortfolioItemDto(symbol: String, rate: String)
  final case class GetOptimalPortfolioResponse(portfolios: Seq[PortfolioItemDto])
  final case class UpdateOptimalPortfolioRequest(portfolios: Seq[PortfolioItemDto])
}

final class PortfolioController(
    getLatestPortfolioUsecase: GetLatestPortfolioUsecase,
    updatePortfolioUsecase: UpdatePortfolioUsecase
)(using ec: ExecutionContext) {
  import PortfolioController.*

  def getOptimalPortfolio(): Future[GetOptimalPortfolioResponse] = {
    getLatestPortfolioUsecase.run().map { out =>
      GetOptimalPortfolioResponse(
        out.items.map(i => PortfolioItemDto(i.symbol.toString, i.rate.toString))
      )
    }
  }

  def updateOptimalPortfolio(req: UpdateOptimalPortfolioRequest): Future[Unit] = {
    val parsed: Either[String, Seq[UpdatePortfolioItemInput]] =
      req.portfolios.foldLeft[Either[String, Seq[UpdatePortfolioItemInput]]](Right(Seq.empty)) { case (acc, dto) =>
        for {
          xs <- acc
          sym <- StockSymbol
            .fromString(dto.symbol)
            .toRight(s"unknown symbol: ${dto.symbol}")
          rate <-
            try Right(BigDecimal(dto.rate))
            catch { case _: Throwable => Left(s"invalid rate: ${dto.rate}") }
        } yield xs :+ UpdatePortfolioItemInput(sym, rate)
      }
    parsed match {
      case Left(msg)    => Future.failed(BadRequestException(msg))
      case Right(items) =>
        updatePortfolioUsecase.run(UpdatePortfolioUsecaseInput(items)).recoverWith {
          case UpdatePortfolioUsecaseException.InvalidPortfolio(reason) =>
            Future.failed(BadRequestException(reason))
        }
    }
  }
}
