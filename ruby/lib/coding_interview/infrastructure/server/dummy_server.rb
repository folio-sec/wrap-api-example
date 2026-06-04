require_relative "../repository/account_repository_impl"
require_relative "../repository/market_price_repository_impl"
require_relative "../repository/portfolio_repository_impl"
require_relative "../../application/usecase/asset/get_asset_usecase"
require_relative "../../application/usecase/portfolio/get_latest_portfolio_usecase"
require_relative "../../application/usecase/portfolio/update_portfolio_usecase"
require_relative "../../application/usecase/market_price/update_market_price_usecase"
require_relative "../../application/usecase/order/new_contribution_order_usecase"
require_relative "../../application/usecase/order/additional_buy_order_usecase"
require_relative "../../application/usecase/order/rebalance_order_usecase"
require_relative "../../presentation/asset_controller"
require_relative "../../presentation/portfolio_controller"
require_relative "../../presentation/order_controller"
require_relative "../../presentation/market_price_controller"

module CodingInterview
  module Infrastructure
    module Server
      class DummyServer
        attr_reader :asset_controller, :portfolio_controller, :order_controller, :market_price_controller

        def initialize(asset_controller, portfolio_controller, order_controller, market_price_controller)
          @asset_controller = asset_controller
          @portfolio_controller = portfolio_controller
          @order_controller = order_controller
          @market_price_controller = market_price_controller
        end

        def self.default
          portfolio_repository = Repository::PortfolioRepositoryImpl.new
          account_repository = Repository::AccountRepositoryImpl.new
          market_price_repository = Repository::MarketPriceRepositoryImpl.new

          get_asset_usecase = Application::Usecase::Asset::GetAssetUsecase.new(account_repository, market_price_repository)
          get_latest_portfolio_usecase = Application::Usecase::Portfolio::GetLatestPortfolioUsecase.new(portfolio_repository)
          update_portfolio_usecase = Application::Usecase::Portfolio::UpdatePortfolioUsecase.new(portfolio_repository)
          update_market_price_usecase = Application::Usecase::MarketPrice::UpdateMarketPriceUsecase.new(market_price_repository)
          new_contribution_order_usecase = Application::Usecase::Order::NewContributionOrderUsecase.new(account_repository, portfolio_repository, market_price_repository)
          additional_buy_order_usecase = Application::Usecase::Order::AdditionalBuyOrderUsecase.new(account_repository, portfolio_repository, market_price_repository)
          rebalance_order_usecase = Application::Usecase::Order::RebalanceOrderUsecase.new(account_repository, portfolio_repository, market_price_repository)

          asset_controller = Presentation::AssetController.new(get_asset_usecase)
          portfolio_controller = Presentation::PortfolioController.new(get_latest_portfolio_usecase, update_portfolio_usecase)
          order_controller = Presentation::OrderController.new(new_contribution_order_usecase, additional_buy_order_usecase, rebalance_order_usecase)
          market_price_controller = Presentation::MarketPriceController.new(update_market_price_usecase)

          new(asset_controller, portfolio_controller, order_controller, market_price_controller)
        end
      end
    end
  end
end
