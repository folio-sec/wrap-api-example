require "bigdecimal"
require_relative "asset_service"
require_relative "../../domain/app_constants"
require_relative "../../domain/stock"

module CodingInterview
  module Application
    module Service
      module PortfolioService
        module_function

        def floor2(x); x.floor(2); end
        def floor0(x); x.floor(0); end

        def price_of(prices, symbol)
          price = prices[symbol]
          raise "missing price for #{symbol}" if price.nil?
          price
        end

        # Allocate a brand-new account given a contribution amount.
        def allocate_new(amount, portfolio, prices)
          cash_from_rate = floor0(amount * Domain::AppConstants::CASH_RATE)
          investable = amount - cash_from_rate
          stocks = portfolio.items.map do |item|
            price = price_of(prices, item.symbol)
            qty = floor2(investable * item.rate / price)
            Domain::Stock.new(item.symbol, qty)
          end
          used_for_stocks = stocks.inject(BigDecimal("0")) { |acc, e| acc + e.qty * price_of(prices, e.symbol) }
          residual = investable - used_for_stocks
          Domain::Account.new(cash_from_rate + residual, stocks)
        end

        # Additional contribution: only buy (no sell). Residual is kept in cash.
        def allocate_additional(account, amount, portfolio, prices)
          total_after = AssetService.total_valuation(account, prices) + amount
          target_cash = floor0(total_after * Domain::AppConstants::CASH_RATE)
          investable = total_after - target_cash
          current_qty = account.stocks.each_with_object({}) { |e, h| h[e.symbol] = e.qty }

          portfolio_symbols = portfolio.items.map(&:symbol)
          new_portfolio_stocks = portfolio.items.map do |item|
            price = price_of(prices, item.symbol)
            target_qty = floor2(investable * item.rate / price)
            current = current_qty.fetch(item.symbol, BigDecimal("0"))
            final_qty = target_qty > current ? target_qty : current
            Domain::Stock.new(item.symbol, final_qty)
          end
          preserved_stocks = account.stocks.reject { |e| portfolio_symbols.include?(e.symbol) }
          all_stocks = new_portfolio_stocks + preserved_stocks

          final_valuation = all_stocks.inject(BigDecimal("0")) { |acc, e| acc + e.qty * price_of(prices, e.symbol) }
          final_cash = total_after - final_valuation
          Domain::Account.new(final_cash, all_stocks)
        end

        # Rebalance: re-allocate qty per portfolio target (buy and sell).
        def rebalance(account, portfolio, prices)
          # XXX this implementation might not be correct
          investable = AssetService.total_valuation(account, prices)
          new_stocks = portfolio.items.map do |item|
            price = price_of(prices, item.symbol)
            qty = floor2(investable * item.rate / price)
            Domain::Stock.new(item.symbol, qty)
          end
          final_valuation = new_stocks.inject(BigDecimal("0")) { |acc, e| acc + e.qty * price_of(prices, e.symbol) }
          final_cash = investable - final_valuation
          Domain::Account.new(final_cash, new_stocks)
        end
      end
    end
  end
end
