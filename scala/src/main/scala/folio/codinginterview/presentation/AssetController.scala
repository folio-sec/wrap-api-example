package folio.codinginterview.presentation

import folio.codinginterview.application.usecase.asset.GetAssetUsecase
import folio.codinginterview.application.usecase.asset.GetAssetUsecaseException
import folio.codinginterview.application.usecase.asset.GetAssetUsecaseInput
import folio.codinginterview.presentation.PresentationException.BadRequestException
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object AssetController {
  final case class StockDto(symbol: String, evaluationAmount: String)
  final case class GetAssetRequest(userId: String)
  final case class GetAssetResponse(cashAmount: String, stocks: Seq[StockDto])
}

final class AssetController(
    getAssetUsecase: GetAssetUsecase
)(using ec: ExecutionContext)
    extends PresentationPreparation {
  import AssetController.*

  def getAsset(req: GetAssetRequest): Future[GetAssetResponse] =
    for {
      uid <- parseUserId(req.userId)
      out <- getAssetUsecase.run(GetAssetUsecaseInput(uid)).recoverWith { case GetAssetUsecaseException.UserNotFound =>
        Future.failed(BadRequestException("user not found"))
      }
    } yield GetAssetResponse(
      cashAmount = out.cashAmount.toString,
      stocks = out.stocks.map(e => StockDto(e.symbol.toString, e.evaluationAmount.toString))
    )
}
