from __future__ import annotations

from dataclasses import dataclass

from coding_interview.application.usecase.asset.get_asset_usecase import GetAssetUsecase
from coding_interview.application.usecase.market_price.update_market_price_usecase import UpdateMarketPriceUsecase
from coding_interview.application.usecase.order.additional_buy_order_usecase import AdditionalBuyOrderUsecase
from coding_interview.application.usecase.order.new_contribution_order_usecase import NewContributionOrderUsecase
from coding_interview.application.usecase.order.rebalance_order_usecase import RebalanceOrderUsecase
from coding_interview.application.usecase.portfolio.get_latest_portfolio_usecase import GetLatestPortfolioUsecase
from coding_interview.application.usecase.portfolio.update_portfolio_usecase import UpdatePortfolioUsecase
from coding_interview.infrastructure.repository.account_repository_impl import AccountRepositoryImpl
from coding_interview.infrastructure.repository.market_price_repository_impl import MarketPriceRepositoryImpl
from coding_interview.infrastructure.repository.portfolio_repository_impl import PortfolioRepositoryImpl
from coding_interview.presentation.asset_controller import AssetController
from coding_interview.presentation.market_price_controller import MarketPriceController
from coding_interview.presentation.order_controller import OrderController
from coding_interview.presentation.portfolio_controller import PortfolioController


@dataclass
class DummyServer:
    asset_controller: AssetController
    portfolio_controller: PortfolioController
    order_controller: OrderController
    market_price_controller: MarketPriceController

    @classmethod
    def default(cls) -> "DummyServer":
        portfolio_repository = PortfolioRepositoryImpl()
        account_repository = AccountRepositoryImpl()
        market_price_repository = MarketPriceRepositoryImpl()

        get_asset_usecase = GetAssetUsecase(account_repository, market_price_repository)
        get_latest_portfolio_usecase = GetLatestPortfolioUsecase(portfolio_repository)
        update_portfolio_usecase = UpdatePortfolioUsecase(portfolio_repository)
        update_market_price_usecase = UpdateMarketPriceUsecase(market_price_repository)
        new_contribution_order_usecase = NewContributionOrderUsecase(
            account_repository, portfolio_repository, market_price_repository
        )
        additional_buy_order_usecase = AdditionalBuyOrderUsecase(
            account_repository, portfolio_repository, market_price_repository
        )
        rebalance_order_usecase = RebalanceOrderUsecase(
            account_repository, portfolio_repository, market_price_repository
        )

        return cls(
            asset_controller=AssetController(get_asset_usecase),
            portfolio_controller=PortfolioController(
                get_latest_portfolio_usecase, update_portfolio_usecase
            ),
            order_controller=OrderController(
                new_contribution_order_usecase,
                additional_buy_order_usecase,
                rebalance_order_usecase,
            ),
            market_price_controller=MarketPriceController(update_market_price_usecase),
        )
