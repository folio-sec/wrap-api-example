require "bigdecimal"
require_relative "presentation_exception"
require_relative "../domain/stock_symbol"
require_relative "../application/usecase/market_price/update_market_price_usecase"

module CodingInterview
  module Presentation
    MarketPriceItemDto = Struct.new(:symbol, :market_price)
    UpdateMarketPriceRequest = Struct.new(:market_prices)

    class MarketPriceController
      def initialize(update_market_price_usecase)
        @update_market_price_usecase = update_market_price_usecase
      end

      def update_market_price(req)
        items = req.market_prices.map do |dto|
          sym = Domain::StockSymbol.from_string(dto.symbol)
          raise BadRequestException.new("unknown symbol: #{dto.symbol}") if sym.nil?
          price =
            begin
              BigDecimal(dto.market_price)
            rescue ArgumentError, TypeError
              raise BadRequestException.new("invalid market_price: #{dto.market_price}")
            end
          Application::Usecase::MarketPrice::UpdateMarketPriceItemInput.new(sym, price)
        end
        @update_market_price_usecase.run(
          Application::Usecase::MarketPrice::UpdateMarketPriceUsecaseInput.new(items)
        )
      end
    end
  end
end
