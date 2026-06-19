import { GetAssetUsecase } from "../../application/usecase/asset/getAssetUsecase.js";
import { AdditionalBuyOrderUsecase } from "../../application/usecase/order/additionalBuyOrderUsecase.js";
import { NewOrderUsecase } from "../../application/usecase/order/newOrderUsecase.js";
import { RebalanceOrderUsecase } from "../../application/usecase/order/rebalanceOrderUsecase.js";
import { GetLatestPortfolioUsecase } from "../../application/usecase/portfolio/getLatestPortfolioUsecase.js";
import { UpdatePortfolioUsecase } from "../../application/usecase/portfolio/updatePortfolioUsecase.js";
import { AssetController } from "../../presentation/assetController.js";
import { OrderController } from "../../presentation/orderController.js";
import { PortfolioController } from "../../presentation/portfolioController.js";
import { AccountRepositoryImpl } from "../repository/accountRepositoryImpl.js";
import { PortfolioRepositoryImpl } from "../repository/portfolioRepositoryImpl.js";

export class DummyServer {
  readonly assetController: AssetController;
  readonly portfolioController: PortfolioController;
  readonly orderController: OrderController;

  constructor(
    assetController: AssetController,
    portfolioController: PortfolioController,
    orderController: OrderController,
  ) {
    this.assetController = assetController;
    this.portfolioController = portfolioController;
    this.orderController = orderController;
  }

  static default(): DummyServer {
    const portfolioRepository = new PortfolioRepositoryImpl();
    const accountRepository = new AccountRepositoryImpl();

    const getAssetUsecase = new GetAssetUsecase(accountRepository);
    const getLatestPortfolioUsecase = new GetLatestPortfolioUsecase(portfolioRepository);
    const updatePortfolioUsecase = new UpdatePortfolioUsecase(portfolioRepository);
    const newOrderUsecase = new NewOrderUsecase(accountRepository, portfolioRepository);
    const additionalBuyOrderUsecase = new AdditionalBuyOrderUsecase(
      accountRepository,
      portfolioRepository,
    );
    const rebalanceOrderUsecase = new RebalanceOrderUsecase(accountRepository, portfolioRepository);

    const assetController = new AssetController(getAssetUsecase);
    const portfolioController = new PortfolioController(
      getLatestPortfolioUsecase,
      updatePortfolioUsecase,
    );
    const orderController = new OrderController(
      newOrderUsecase,
      additionalBuyOrderUsecase,
      rebalanceOrderUsecase,
    );

    return new DummyServer(assetController, portfolioController, orderController);
  }
}
