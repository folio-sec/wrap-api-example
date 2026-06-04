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
        PortfolioRepositoryImpl portfolioRepository = new PortfolioRepositoryImpl();
        AccountRepositoryImpl accountRepository = new AccountRepositoryImpl();
        MarketPriceRepositoryImpl marketPriceRepository = new MarketPriceRepositoryImpl();

        GetAssetUsecase getAssetUsecase = new GetAssetUsecase(accountRepository, marketPriceRepository);
        GetLatestPortfolioUsecase getLatestPortfolioUsecase = new GetLatestPortfolioUsecase(portfolioRepository);
        UpdatePortfolioUsecase updatePortfolioUsecase = new UpdatePortfolioUsecase(portfolioRepository);
        UpdateMarketPriceUsecase updateMarketPriceUsecase = new UpdateMarketPriceUsecase(marketPriceRepository);
        NewContributionOrderUsecase newContributionOrderUsecase = new NewContributionOrderUsecase(
                accountRepository, portfolioRepository, marketPriceRepository);
        AdditionalBuyOrderUsecase additionalBuyOrderUsecase = new AdditionalBuyOrderUsecase(
                accountRepository, portfolioRepository, marketPriceRepository);
        RebalanceOrderUsecase rebalanceOrderUsecase = new RebalanceOrderUsecase(
                accountRepository, portfolioRepository, marketPriceRepository);

        AssetController assetController = new AssetController(getAssetUsecase);
        PortfolioController portfolioController = new PortfolioController(getLatestPortfolioUsecase, updatePortfolioUsecase);
        OrderController orderController = new OrderController(
                newContributionOrderUsecase, additionalBuyOrderUsecase, rebalanceOrderUsecase);
        MarketPriceController marketPriceController = new MarketPriceController(updateMarketPriceUsecase);

        return new DummyServer(assetController, portfolioController, orderController, marketPriceController);
    }
}
