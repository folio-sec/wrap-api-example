require_relative "../../application/repository/market_price_repository"
require_relative "../../domain/app_constants"

module CodingInterview
  module Infrastructure
    module Repository
      class MarketPriceRepositoryImpl < Application::Repository::MarketPriceRepository
        def initialize
          @prices = Domain::AppConstants::INITIAL_PRICES.dup
          @mutex = Mutex.new
        end

        def all
          @mutex.synchronize { @prices.dup }
        end

        def update(prices)
          @mutex.synchronize { @prices = prices.dup }
          nil
        end
      end
    end
  end
end
