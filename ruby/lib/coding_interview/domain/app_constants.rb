require "bigdecimal"
require_relative "stock"
require_relative "stock_symbol"

module CodingInterview
  module Domain
    module AppConstants
      CASH_RATE = BigDecimal("0.05")
      MIN_OPERATION_AMOUNT = BigDecimal("10000")

      SUPPORTED_SYMBOLS = [StockSymbol::Toyopa, StockSymbol::Somy].freeze

      INITIAL_PRICES = {
        StockSymbol::Toyopa => BigDecimal("4.2135"),
        StockSymbol::Somy => BigDecimal("1.2345")
      }.freeze

      INITIAL_PORTFOLIO = Portfolio.new([
        PortfolioItem.new(StockSymbol::Toyopa, BigDecimal("0.40")),
        PortfolioItem.new(StockSymbol::Somy, BigDecimal("0.60"))
      ])
    end
  end
end
