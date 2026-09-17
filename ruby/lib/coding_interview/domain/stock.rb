require "bigdecimal"

module CodingInterview
  module Domain
    # Stock は保有銘柄（銘柄と保有額）を表す。
    Stock = Struct.new(:symbol, :amount_jpy)

    # PortfolioItem は最適ポートフォリオの1銘柄エントリー（銘柄と構成比率）を表す。
    PortfolioItem = Struct.new(:symbol, :rate)

    # Portfolio は最適ポートフォリオ（銘柄ごとの構成比率）を表す。
    class Portfolio
      attr_reader :items

      def initialize(items)
        raise ArgumentError, "portfolio must have at least one item" if items.empty?
        total = items.map(&:rate).inject(BigDecimal("0")) { |acc, r| acc + r }
        raise ArgumentError, "portfolio rates must sum to 1, got #{total}" unless total == BigDecimal("1")
        symbols = items.map(&:symbol)
        raise ArgumentError, "portfolio must not have duplicate symbols" if symbols.uniq.size != symbols.size
        @items = items.freeze
      end
    end

  end
end
