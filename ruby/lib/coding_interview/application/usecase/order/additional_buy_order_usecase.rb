require_relative "../../service/portfolio_service"
require_relative "../../../domain/app_constants"

module CodingInterview
  module Application
    module Usecase
      module Order
        AdditionalBuyOrderUsecaseInput = Struct.new(:user_id, :amount)

        class AdditionalBuyOrderUsecaseException < StandardError; end
        class AdditionalBuyUserNotFound < AdditionalBuyOrderUsecaseException; end
        class AdditionalBuyAmountTooSmall < AdditionalBuyOrderUsecaseException; end

        class AdditionalBuyOrderUsecase
          def initialize(account_repository, portfolio_repository, market_price_repository)
            @account_repository = account_repository
            @portfolio_repository = portfolio_repository
            @market_price_repository = market_price_repository
          end

          def run(input)
            raise AdditionalBuyAmountTooSmall if input.amount < Domain::AppConstants::MIN_OPERATION_AMOUNT
            account = @account_repository.find(input.user_id)
            raise AdditionalBuyUserNotFound if account.nil?

            portfolio = @portfolio_repository.get
            prices = @market_price_repository.all
            updated = Service::PortfolioService.allocate_additional(account, input.amount, portfolio, prices)
            @account_repository.upsert(input.user_id, updated)
          end
        end
      end
    end
  end
end
