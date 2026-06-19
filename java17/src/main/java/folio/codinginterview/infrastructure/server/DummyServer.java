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
        var portfolioRepository = new PortfolioRepositoryImpl();
        var accountRepository = new AccountRepositoryImpl();

        var getAssetUsecase = new GetAssetUsecase(accountRepository);
        var getLatestPortfolioUsecase = new GetLatestPortfolioUsecase(portfolioRepository);
        var updatePortfolioUsecase = new UpdatePortfolioUsecase(portfolioRepository);
        var newOrderUsecase = new NewOrderUsecase(accountRepository, portfolioRepository);
        var additionalBuyOrderUsecase = new AdditionalBuyOrderUsecase(accountRepository, portfolioRepository);
        var rebalanceOrderUsecase = new RebalanceOrderUsecase(accountRepository, portfolioRepository);

        var assetController = new AssetController(getAssetUsecase);
        var portfolioController = new PortfolioController(getLatestPortfolioUsecase, updatePortfolioUsecase);
        var orderController = new OrderController(newOrderUsecase, additionalBuyOrderUsecase, rebalanceOrderUsecase);

        return new DummyServer(assetController, portfolioController, orderController);
    }
}
