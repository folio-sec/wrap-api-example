module CodingInterview
  module Application
    module Usecase
      module MarketPrice
        UpdateMarketPriceItemInput = Struct.new(:symbol, :market_price)
        UpdateMarketPriceUsecaseInput = Struct.new(:items)

        class UpdateMarketPriceUsecase
          def initialize(market_price_repository)
            @market_price_repository = market_price_repository
          end

          def run(input)
            prices = input.items.each_with_object({}) { |i, h| h[i.symbol] = i.market_price }
            @market_price_repository.update(prices)
          end
        end
      end
    end
  end
end
