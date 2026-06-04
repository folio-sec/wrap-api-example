module CodingInterview
  module Application
    module Repository
      # 最適ポートフォリオリポジトリ。
      class PortfolioRepository
        def get; raise NotImplementedError; end
        def update(_portfolio); raise NotImplementedError; end
      end
    end
  end
end
