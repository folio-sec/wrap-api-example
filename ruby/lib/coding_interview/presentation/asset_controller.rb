require_relative "presentation_exception"
require_relative "presentation_preparation"
require_relative "../application/usecase/asset/get_asset_usecase"

module CodingInterview
  module Presentation
    StockDto = Struct.new(:symbol, :evaluation_amount)
    GetAssetRequest = Struct.new(:user_id)
    GetAssetResponse = Struct.new(:cash_amount, :stocks)

    class AssetController
      include PresentationPreparation

      def initialize(get_asset_usecase)
        @get_asset_usecase = get_asset_usecase
      end

      def get_asset(req)
        uid = parse_user_id(req.user_id)
        out =
          begin
            @get_asset_usecase.run(Application::Usecase::Asset::GetAssetUsecaseInput.new(uid))
          rescue Application::Usecase::Asset::UserNotFound
            raise BadRequestException.new("user not found")
          end
        GetAssetResponse.new(
          out.cash_amount.to_s("F"),
          out.stocks.map { |e| StockDto.new(e.symbol.to_s, e.evaluation_amount.to_s("F")) }
        )
      end
    end
  end
end
