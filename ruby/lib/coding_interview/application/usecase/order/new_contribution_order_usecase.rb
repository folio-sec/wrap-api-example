require_relative "../../service/portfolio_service"
require_relative "../../../domain/app_constants"

module CodingInterview
  module Application
    module Usecase
      module Order
        NewContributionOrderUsecaseInput = Struct.new(:user_id, :amount)

        class NewContributionOrderUsecaseException < StandardError; end
        class UserAlreadyExists < NewContributionOrderUsecaseException; end
        class NewContributionAmountTooSmall < NewContributionOrderUsecaseException; end

        class NewContributionOrderUsecase
          def initialize(account_repository, portfolio_repository, market_price_repository)
            @account_repository = account_repository
            @portfolio_repository = portfolio_repository
            @market_price_repository = market_price_repository
          end

          def run(input)
            raise NewContributionAmountTooSmall if input.amount < Domain::AppConstants::MIN_OPERATION_AMOUNT
            raise UserAlreadyExists if @account_repository.exists?(input.user_id)

            portfolio = @portfolio_repository.get
            prices = @market_price_repository.all
            account = Service::PortfolioService.allocate_new(input.amount, portfolio, prices)
            @account_repository.upsert(input.user_id, account)
          end
        end
      end
    end
  end
end
