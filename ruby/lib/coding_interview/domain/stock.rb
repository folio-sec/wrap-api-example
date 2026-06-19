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

    # Account は口座を表す。
    Account = Struct.new(:cash, :stocks) do
      def self.floor0(x)
        x.truncate(0)
      end

      # total は口座の総資産（現金 + 各銘柄の保有額）を返す。
      def total
        stocks.inject(cash) { |acc, s| acc + s.amount_jpy }
      end

      # open_account は新規注文額を、最適ポートフォリオに沿って配分した口座を生成する。
      def self.open_account(amount, portfolio)
        cash_from_rate = floor0(amount * AppConstants::CASH_RATE)
        investable = amount - cash_from_rate

        used_for_stocks = BigDecimal("0")
        new_stocks = portfolio.items.map do |item|
          amt = floor0(investable * item.rate)
          used_for_stocks += amt
          Stock.new(item.symbol, amt)
        end

        residual = investable - used_for_stocks
        new(cash_from_rate + residual, new_stocks)
      end

      # add_funds は追加注文額を口座へ反映する。最適ポートフォリオの目標額を下回らない範囲で
      # 既存の保有額を維持し、ポートフォリオ外の銘柄はそのまま保持する。
      def add_funds(amount, portfolio)
        total_after = total + amount
        target_cash = Account.floor0(total_after * AppConstants::CASH_RATE)
        investable = total_after - target_cash

        current_amount = stocks.each_with_object({}) { |e, h| h[e.symbol] = e.amount_jpy }
        portfolio_symbols = portfolio.items.map(&:symbol)

        new_portfolio_stocks = portfolio.items.map do |item|
          target = Account.floor0(investable * item.rate)
          current = current_amount.fetch(item.symbol, BigDecimal("0"))
          final = current > target ? current : target
          Stock.new(item.symbol, final)
        end

        preserved_stocks = stocks.reject { |e| portfolio_symbols.include?(e.symbol) }
        all_stocks = new_portfolio_stocks + preserved_stocks

        final_amount = all_stocks.inject(BigDecimal("0")) { |acc, e| acc + e.amount_jpy }
        final_cash = total_after - final_amount
        Account.new(final_cash, all_stocks)
      end

      # rebalance は保有資産を最適ポートフォリオの比率に近づける。
      def rebalance(portfolio)
        # XXX this implementation might not be correct
        investable = total

        used_for_stocks = BigDecimal("0")
        new_stocks = portfolio.items.map do |item|
          amt = Account.floor0(investable * item.rate)
          used_for_stocks += amt
          Stock.new(item.symbol, amt)
        end

        final_cash = investable - used_for_stocks
        Account.new(final_cash, new_stocks)
      end
    end
  end
end
