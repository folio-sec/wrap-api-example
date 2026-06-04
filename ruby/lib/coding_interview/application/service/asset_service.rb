require "bigdecimal"

module CodingInterview
  module Application
    module Service
      module AssetService
        module_function

        def evaluate_stock(stock, prices)
          price = prices[stock.symbol]
          raise "missing price for #{stock.symbol}" if price.nil?
          stock.qty * price
        end

        def total_valuation(account, prices)
          account.stocks.inject(account.cash) { |acc, e| acc + evaluate_stock(e, prices) }
        end
      end
    end
  end
end
