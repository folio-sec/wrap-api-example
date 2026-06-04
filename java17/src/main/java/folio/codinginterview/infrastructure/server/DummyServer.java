package folio.codinginterview.infrastructure.server;

import folio.codinginterview.application.usecase.asset.GetAssetUsecase;
import folio.codinginterview.application.usecase.market_price.UpdateMarketPriceUsecase;
import folio.codinginterview.application.usecase.order.AdditionalBuyOrderUsecase;
import folio.codinginterview.application.usecase.order.NewContributionOrderUsecase;
import folio.codinginterview.application.usecase.order.RebalanceOrderUsecase;
import folio.codinginterview.application.usecase.portfolio.GetLatestPortfolioUsecase;
import folio.codinginterview.application.usecase.portfolio.UpdatePortfolioUsecase;
import folio.codinginterview.infrastructure.repository.AccountRepositoryImpl;
import folio.codinginterview.infrastructure.repository.MarketPriceRepositoryImpl;
import folio.codinginterview.infrastructure.repository.PortfolioRepositoryImpl;
import folio.codinginterview.presentation.AssetController;
import folio.codinginterview.presentation.MarketPriceController;
import folio.codinginterview.presentation.OrderController;
import folio.codinginterview.presentation.PortfolioController;

public final class DummyServer {
    private final AssetController assetController;
    private final PortfolioController portfolioController;
    private final OrderController orderController;
    private final MarketPriceController marketPriceController;

    public DummyServer(
            AssetController assetController,
            PortfolioController portfolioController,
            OrderController orderController,
            MarketPriceController marketPriceController
    ) {
        this.assetController = assetController;
        this.portfolioController = portfolioController;
        this.orderController = orderController;
        this.marketPriceController = marketPriceController;
    }

    public AssetController assetController() { return assetController; }

    public PortfolioController portfolioController() { return portfolioController; }

    public OrderController orderController() { return orderController; }

    public MarketPriceController marketPriceController() { return marketPriceController; }

    public static DummyServer defaultInstance() {
        var portfolioRepository = new PortfolioRepositoryImpl();
        var accountRepository = new AccountRepositoryImpl();
        var marketPriceRepository = new MarketPriceRepositoryImpl();

        var getAssetUsecase = new GetAssetUsecase(accountRepository, marketPriceRepository);
        var getLatestPortfolioUsecase = new GetLatestPortfolioUsecase(portfolioRepository);
        var updatePortfolioUsecase = new UpdatePortfolioUsecase(portfolioRepository);
        var updateMarketPriceUsecase = new UpdateMarketPriceUsecase(marketPriceRepository);
        var newContributionOrderUsecase = new NewContributionOrderUsecase(
                accountRepository, portfolioRepository, marketPriceRepository);
        var additionalBuyOrderUsecase = new AdditionalBuyOrderUsecase(
                accountRepository, portfolioRepository, marketPriceRepository);
        var rebalanceOrderUsecase = new RebalanceOrderUsecase(
                accountRepository, portfolioRepository, marketPriceRepository);

        var assetController = new AssetController(getAssetUsecase);
        var portfolioController = new PortfolioController(getLatestPortfolioUsecase, updatePortfolioUsecase);
        var orderController = new OrderController(
                newContributionOrderUsecase, additionalBuyOrderUsecase, rebalanceOrderUsecase);
        var marketPriceController = new MarketPriceController(updateMarketPriceUsecase);

        return new DummyServer(assetController, portfolioController, orderController, marketPriceController);
    }
}
