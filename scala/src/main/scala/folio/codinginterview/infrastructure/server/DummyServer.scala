package folio.codinginterview.infrastructure.server

import folio.codinginterview.application.usecase.asset.GetAssetUsecase
import folio.codinginterview.application.usecase.order.AdditionalBuyOrderUsecase
import folio.codinginterview.application.usecase.order.NewOrderUsecase
import folio.codinginterview.application.usecase.order.RebalanceOrderUsecase
import folio.codinginterview.application.usecase.portfolio.GetLatestPortfolioUsecase
import folio.codinginterview.application.usecase.portfolio.UpdatePortfolioUsecase
import folio.codinginterview.infrastructure.repository.AccountRepositoryImpl
import folio.codinginterview.infrastructure.repository.PortfolioRepositoryImpl
import folio.codinginterview.presentation.AssetController
import folio.codinginterview.presentation.OrderController
import folio.codinginterview.presentation.PortfolioController
import scala.concurrent.ExecutionContext

final class DummyServer(
    val assetController: AssetController,
    val portfolioController: PortfolioController,
    val orderController: OrderController
)

object DummyServer {
  def default()(using ec: ExecutionContext): DummyServer = {
    val portfolioRepository = new PortfolioRepositoryImpl
    val accountRepository = new AccountRepositoryImpl

    val getAssetUsecase = new GetAssetUsecase(accountRepository)
    val getLatestPortfolioUsecase = new GetLatestPortfolioUsecase(portfolioRepository)
    val updatePortfolioUsecase = new UpdatePortfolioUsecase(portfolioRepository)
    val newOrderUsecase = new NewOrderUsecase(accountRepository, portfolioRepository)
    val additionalBuyOrderUsecase = new AdditionalBuyOrderUsecase(
      accountRepository,
      portfolioRepository
    )
    val rebalanceOrderUsecase = new RebalanceOrderUsecase(accountRepository, portfolioRepository)

    val assetController = new AssetController(getAssetUsecase)
    val portfolioController = new PortfolioController(
      getLatestPortfolioUsecase,
      updatePortfolioUsecase
    )
    val orderController = new OrderController(
      newOrderUsecase,
      additionalBuyOrderUsecase,
      rebalanceOrderUsecase
    )

    new DummyServer(assetController, portfolioController, orderController)
  }
}
