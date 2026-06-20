require_relative "../../../domain/app_constants"
require_relative "../../../domain/stock"

module CodingInterview
  module Application
    module Usecase
      module Order
        NewOrderUsecaseInput = Struct.new(:user_id, :amount)

        class NewOrderUsecaseException < StandardError; end
        class NewOrderUserAlreadyExistsError < NewOrderUsecaseException; end
        class NewOrderAmountTooSmallError < NewOrderUsecaseException; end

        class NewOrderUsecase
          def initialize(account_repository, portfolio_repository)
            @account_repository = account_repository
            @portfolio_repository = portfolio_repository
          end

          def run(input)
            raise NewOrderAmountTooSmallError if input.amount < Domain::AppConstants::MIN_OPERATION_AMOUNT
            raise NewOrderUserAlreadyExistsError if @account_repository.exists?(input.user_id)

            portfolio = @portfolio_repository.get
            account = Domain::Account.open_account(input.amount, portfolio)
            @account_repository.upsert(input.user_id, account)
          end
        end
      end
    end
  end
end
