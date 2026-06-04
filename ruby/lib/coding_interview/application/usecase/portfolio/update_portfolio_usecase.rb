require_relative "../../../domain/stock"

module CodingInterview
  module Application
    module Usecase
      module Portfolio
        UpdatePortfolioItemInput = Struct.new(:symbol, :rate)
        UpdatePortfolioUsecaseInput = Struct.new(:items)

        class UpdatePortfolioUsecaseException < StandardError; end
        class InvalidPortfolio < UpdatePortfolioUsecaseException
          def initialize(reason)
            super(reason)
            @reason = reason
          end
          attr_reader :reason
        end

        class UpdatePortfolioUsecase
          def initialize(portfolio_repository)
            @portfolio_repository = portfolio_repository
          end

          def run(input)
            portfolio =
              begin
                Domain::Portfolio.new(
                  input.items.map { |i| Domain::PortfolioItem.new(i.symbol, i.rate) }
                )
              rescue ArgumentError => e
                raise InvalidPortfolio.new(e.message)
              end
            @portfolio_repository.update(portfolio)
          end
        end
      end
    end
  end
end
