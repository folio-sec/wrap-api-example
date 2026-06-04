import { GetAssetUsecase } from "../../application/usecase/asset/getAssetUsecase.js";
import { UpdateMarketPriceUsecase } from "../../application/usecase/market_price/updateMarketPriceUsecase.js";
import { AdditionalBuyOrderUsecase } from "../../application/usecase/order/additionalBuyOrderUsecase.js";
import { NewContributionOrderUsecase } from "../../application/usecase/order/newContributionOrderUsecase.js";
import { RebalanceOrderUsecase } from "../../application/usecase/order/rebalanceOrderUsecase.js";
import { GetLatestPortfolioUsecase } from "../../application/usecase/portfolio/getLatestPortfolioUsecase.js";
import { UpdatePortfolioUsecase } from "../../application/usecase/portfolio/updatePortfolioUsecase.js";
import { AssetController } from "../../presentation/assetController.js";
import { MarketPriceController } from "../../presentation/marketPriceController.js";
import { OrderController } from "../../presentation/orderController.js";
import { PortfolioController } from "../../presentation/portfolioController.js";
import { AccountRepositoryImpl } from "../repository/accountRepositoryImpl.js";
import { MarketPriceRepositoryImpl } from "../repository/marketPriceRepositoryImpl.js";
import { PortfolioRepositoryImpl } from "../repository/portfolioRepositoryImpl.js";

export class DummyServer {
  readonly assetController: AssetController;
  readonly portfolioController: PortfolioController;
  readonly orderController: OrderController;
  readonly marketPriceController: MarketPriceController;

  constructor(
    assetController: AssetController,
    portfolioController: PortfolioController,
    orderController: OrderController,
    marketPriceController: MarketPriceController,
  ) {
    this.assetController = assetController;
    this.portfolioController = portfolioController;
    this.orderController = orderController;
    this.marketPriceController = marketPriceController;
  }

  static default(): DummyServer {
    const portfolioRepository = new PortfolioRepositoryImpl();
    const accountRepository = new AccountRepositoryImpl();
    const marketPriceRepository = new MarketPriceRepositoryImpl();

    const getAssetUsecase = new GetAssetUsecase(accountRepository, marketPriceRepository);
    const getLatestPortfolioUsecase = new GetLatestPortfolioUsecase(portfolioRepository);
    const updatePortfolioUsecase = new UpdatePortfolioUsecase(portfolioRepository);
    const updateMarketPriceUsecase = new UpdateMarketPriceUsecase(marketPriceRepository);
    const newContributionOrderUsecase = new NewContributionOrderUsecase(
      accountRepository,
      portfolioRepository,
      marketPriceRepository,
    );
    const additionalBuyOrderUsecase = new AdditionalBuyOrderUsecase(
      accountRepository,
      portfolioRepository,
      marketPriceRepository,
    );
    const rebalanceOrderUsecase = new RebalanceOrderUsecase(
      accountRepository,
      portfolioRepository,
      marketPriceRepository,
    );

    const assetController = new AssetController(getAssetUsecase);
    const portfolioController = new PortfolioController(
      getLatestPortfolioUsecase,
      updatePortfolioUsecase,
    );
    const orderController = new OrderController(
      newContributionOrderUsecase,
      additionalBuyOrderUsecase,
      rebalanceOrderUsecase,
    );
    const marketPriceController = new MarketPriceController(updateMarketPriceUsecase);

    return new DummyServer(
      assetController,
      portfolioController,
      orderController,
      marketPriceController,
    );
  }
}
