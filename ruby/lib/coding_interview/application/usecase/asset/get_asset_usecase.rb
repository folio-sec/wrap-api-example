require_relative "../../service/asset_service"

module CodingInterview
  module Application
    module Usecase
      module Asset
        GetAssetUsecaseInput = Struct.new(:user_id)
        GetAssetStockOutput = Struct.new(:symbol, :evaluation_amount)
        GetAssetUsecaseOutput = Struct.new(:cash_amount, :stocks)

        class GetAssetUsecaseException < StandardError; end
        class UserNotFound < GetAssetUsecaseException; end

        class GetAssetUsecase
          def initialize(account_repository, market_price_repository)
            @account_repository = account_repository
            @market_price_repository = market_price_repository
          end

          def run(input)
            account = @account_repository.find(input.user_id)
            raise UserNotFound if account.nil?
            prices = @market_price_repository.all
            stocks = account.stocks.map do |e|
              GetAssetStockOutput.new(e.symbol, Service::AssetService.evaluate_stock(e, prices))
            end
            GetAssetUsecaseOutput.new(account.cash, stocks)
          end
        end
      end
    end
  end
end
