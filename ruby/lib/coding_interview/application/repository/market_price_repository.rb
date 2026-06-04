module CodingInterview
  module Application
    module Repository
      # 市場価格リポジトリ。
      class MarketPriceRepository
        def all; raise NotImplementedError; end
        def update(_prices); raise NotImplementedError; end
      end
    end
  end
end
