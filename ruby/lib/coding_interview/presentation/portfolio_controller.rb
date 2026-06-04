require "bigdecimal"
require_relative "presentation_exception"
require_relative "../domain/stock_symbol"
require_relative "../application/usecase/portfolio/get_latest_portfolio_usecase"
require_relative "../application/usecase/portfolio/update_portfolio_usecase"

module CodingInterview
  module Presentation
    PortfolioItemDto = Struct.new(:symbol, :rate)
    GetOptimalPortfolioResponse = Struct.new(:portfolios)
    UpdateOptimalPortfolioRequest = Struct.new(:portfolios)

    class PortfolioController
      def initialize(get_latest_portfolio_usecase, update_portfolio_usecase)
        @get_latest_portfolio_usecase = get_latest_portfolio_usecase
        @update_portfolio_usecase = update_portfolio_usecase
      end

      def get_optimal_portfolio
        out = @get_latest_portfolio_usecase.run
        GetOptimalPortfolioResponse.new(
          out.items.map { |i| PortfolioItemDto.new(i.symbol.to_s, i.rate.to_s("F")) }
        )
      end

      def update_optimal_portfolio(req)
        items = req.portfolios.map do |dto|
          sym = Domain::StockSymbol.from_string(dto.symbol)
          raise BadRequestException.new("unknown symbol: #{dto.symbol}") if sym.nil?
          rate =
            begin
              BigDecimal(dto.rate)
            rescue ArgumentError, TypeError
              raise BadRequestException.new("invalid rate: #{dto.rate}")
            end
          Application::Usecase::Portfolio::UpdatePortfolioItemInput.new(sym, rate)
        end
        begin
          @update_portfolio_usecase.run(Application::Usecase::Portfolio::UpdatePortfolioUsecaseInput.new(items))
        rescue Application::Usecase::Portfolio::InvalidPortfolio => e
          raise BadRequestException.new(e.reason)
        end
      end
    end
  end
end
