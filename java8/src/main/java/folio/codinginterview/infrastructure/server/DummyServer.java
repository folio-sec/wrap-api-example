package folio.codinginterview.infrastructure.server;

import folio.codinginterview.application.usecase.asset.GetAssetUsecase;
import folio.codinginterview.application.usecase.order.AdditionalBuyOrderUsecase;
import folio.codinginterview.application.usecase.order.NewOrderUsecase;
import folio.codinginterview.application.usecase.order.RebalanceOrderUsecase;
import folio.codinginterview.application.usecase.portfolio.GetLatestPortfolioUsecase;
import folio.codinginterview.application.usecase.portfolio.UpdatePortfolioUsecase;
import folio.codinginterview.infrastructure.repository.AccountRepositoryImpl;
import folio.codinginterview.infrastructure.repository.PortfolioRepositoryImpl;
import folio.codinginterview.presentation.AssetController;
import folio.codinginterview.presentation.OrderController;
import folio.codinginterview.presentation.PortfolioController;

public final class DummyServer {
    private final AssetController assetController;
    private final PortfolioController portfolioController;
    private final OrderController orderController;

    public DummyServer(
            AssetController assetController,
            PortfolioController portfolioController,
            OrderController orderController
    ) {
        this.assetController = assetController;
        this.portfolioController = portfolioController;
        this.orderController = orderController;
    }

    public AssetController assetController() { return assetController; }

    public PortfolioController portfolioController() { return portfolioController; }

    public OrderController orderController() { return orderController; }

    public static DummyServer defaultInstance() {
        PortfolioRepositoryImpl portfolioRepository = new PortfolioRepositoryImpl();
        AccountRepositoryImpl accountRepository = new AccountRepositoryImpl();

        GetAssetUsecase getAssetUsecase = new GetAssetUsecase(accountRepository);
        GetLatestPortfolioUsecase getLatestPortfolioUsecase = new GetLatestPortfolioUsecase(portfolioRepository);
        UpdatePortfolioUsecase updatePortfolioUsecase = new UpdatePortfolioUsecase(portfolioRepository);
        NewOrderUsecase newOrderUsecase = new NewOrderUsecase(accountRepository, portfolioRepository);
        AdditionalBuyOrderUsecase additionalBuyOrderUsecase = new AdditionalBuyOrderUsecase(accountRepository, portfolioRepository);
        RebalanceOrderUsecase rebalanceOrderUsecase = new RebalanceOrderUsecase(accountRepository, portfolioRepository);

        AssetController assetController = new AssetController(getAssetUsecase);
        PortfolioController portfolioController = new PortfolioController(getLatestPortfolioUsecase, updatePortfolioUsecase);
        OrderController orderController = new OrderController(newOrderUsecase, additionalBuyOrderUsecase, rebalanceOrderUsecase);

        return new DummyServer(assetController, portfolioController, orderController);
    }
}
