module CodingInterview
  module Application
    module Usecase
      module Asset
        GetAssetUsecaseInput = Struct.new(:user_id)
        GetAssetStockOutput = Struct.new(:symbol, :amount_jpy)
        GetAssetUsecaseOutput = Struct.new(:cash_amount, :stocks)

        class GetAssetUsecaseException < StandardError; end
        class UserNotFound < GetAssetUsecaseException; end

        class GetAssetUsecase
          def initialize(account_repository)
            @account_repository = account_repository
          end

          def run(input)
            account = @account_repository.find(input.user_id)
            raise UserNotFound if account.nil?
            stocks = account.stocks.map do |e|
              GetAssetStockOutput.new(e.symbol, e.amount_jpy)
            end
            GetAssetUsecaseOutput.new(account.cash, stocks)
          end
        end
      end
    end
  end
end
