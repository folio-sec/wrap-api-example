package folio.codinginterview.infrastructure.server

import folio.codinginterview.application.usecase.asset.GetAssetUsecase
import folio.codinginterview.application.usecase.market_price.UpdateMarketPriceUsecase
import folio.codinginterview.application.usecase.order.AdditionalBuyOrderUsecase
import folio.codinginterview.application.usecase.order.NewContributionOrderUsecase
import folio.codinginterview.application.usecase.order.RebalanceOrderUsecase
import folio.codinginterview.application.usecase.portfolio.GetLatestPortfolioUsecase
import folio.codinginterview.application.usecase.portfolio.UpdatePortfolioUsecase
import folio.codinginterview.infrastructure.repository.AccountRepositoryImpl
import folio.codinginterview.infrastructure.repository.MarketPriceRepositoryImpl
import folio.codinginterview.infrastructure.repository.PortfolioRepositoryImpl
import folio.codinginterview.presentation.AssetController
import folio.codinginterview.presentation.MarketPriceController
import folio.codinginterview.presentation.OrderController
import folio.codinginterview.presentation.PortfolioController
import scala.concurrent.ExecutionContext

final class DummyServer(
    val assetController: AssetController,
    val portfolioController: PortfolioController,
    val orderController: OrderController,
    val marketPriceController: MarketPriceController
)

object DummyServer {
  def default()(using ec: ExecutionContext): DummyServer = {
    val portfolioRepository = new PortfolioRepositoryImpl
    val accountRepository = new AccountRepositoryImpl
    val marketPriceRepository = new MarketPriceRepositoryImpl

    val getAssetUsecase = new GetAssetUsecase(accountRepository, marketPriceRepository)
    val getLatestPortfolioUsecase = new GetLatestPortfolioUsecase(portfolioRepository)
    val updatePortfolioUsecase = new UpdatePortfolioUsecase(portfolioRepository)
    val updateMarketPriceUsecase = new UpdateMarketPriceUsecase(marketPriceRepository)
    val newContributionOrderUsecase = new NewContributionOrderUsecase(
      accountRepository,
      portfolioRepository,
      marketPriceRepository
    )
    val additionalBuyOrderUsecase = new AdditionalBuyOrderUsecase(
      accountRepository,
      portfolioRepository,
      marketPriceRepository
    )
    val rebalanceOrderUsecase = new RebalanceOrderUsecase(
      accountRepository,
      portfolioRepository,
      marketPriceRepository
    )

    val assetController = new AssetController(getAssetUsecase)
    val portfolioController = new PortfolioController(
      getLatestPortfolioUsecase,
      updatePortfolioUsecase
    )
    val orderController = new OrderController(
      newContributionOrderUsecase,
      additionalBuyOrderUsecase,
      rebalanceOrderUsecase
    )
    val marketPriceController = new MarketPriceController(updateMarketPriceUsecase)

    new DummyServer(assetController, portfolioController, orderController, marketPriceController)
  }
}
