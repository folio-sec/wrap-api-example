module CodingInterview
  module Application
    module Usecase
      module Order
        RebalanceOrderUsecaseInput = Struct.new(:user_id)

        class RebalanceOrderUsecaseException < StandardError; end
        class RebalanceUserNotFound < RebalanceOrderUsecaseException; end

        class RebalanceOrderUsecase
          def initialize(account_repository, portfolio_repository)
            @account_repository = account_repository
            @portfolio_repository = portfolio_repository
          end

          def run(input)
            account = @account_repository.find(input.user_id)
            raise RebalanceUserNotFound if account.nil?

            portfolio = @portfolio_repository.get
            updated = account.rebalance(portfolio)
            @account_repository.upsert(input.user_id, updated)
          end
        end
      end
    end
  end
end
