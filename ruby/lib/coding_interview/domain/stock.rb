module CodingInterview
  module Domain
    Stock = Struct.new(:symbol, :qty)
    PortfolioItem = Struct.new(:symbol, :rate)

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

    Account = Struct.new(:cash, :stocks)
  end
end
