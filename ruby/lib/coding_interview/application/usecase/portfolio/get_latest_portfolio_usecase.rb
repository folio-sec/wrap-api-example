module CodingInterview
  module Application
    module Usecase
      module Portfolio
        GetLatestPortfolioItemOutput = Struct.new(:symbol, :rate)
        GetLatestPortfolioUsecaseOutput = Struct.new(:items)

        class GetLatestPortfolioUsecase
          def initialize(portfolio_repository)
            @portfolio_repository = portfolio_repository
          end

          def run
            p = @portfolio_repository.get
            GetLatestPortfolioUsecaseOutput.new(
              p.items.map { |i| GetLatestPortfolioItemOutput.new(i.symbol, i.rate) }
            )
          end
        end
      end
    end
  end
end
