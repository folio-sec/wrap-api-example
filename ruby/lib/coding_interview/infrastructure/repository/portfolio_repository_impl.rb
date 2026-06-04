require_relative "../../application/repository/portfolio_repository"
require_relative "../../domain/app_constants"

module CodingInterview
  module Infrastructure
    module Repository
      class PortfolioRepositoryImpl < Application::Repository::PortfolioRepository
        def initialize
          @portfolio = Domain::AppConstants::INITIAL_PORTFOLIO
          @mutex = Mutex.new
        end

        def get
          @mutex.synchronize { @portfolio }
        end

        def update(portfolio)
          @mutex.synchronize { @portfolio = portfolio }
          nil
        end
      end
    end
  end
end
