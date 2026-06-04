import Decimal from "decimal.js";
import { Account, Stock } from "../../domain/stock.js";
import { StockSymbol } from "../../domain/stockSymbol.js";

export const AssetService = {
  evaluateStock(stock: Stock, prices: Map<StockSymbol, Decimal>): Decimal {
    const price = prices.get(stock.symbol);
    if (price === undefined) {
      throw new Error(`missing price for ${stock.symbol}`);
    }
    return stock.qty.times(price);
  },

  totalValuation(account: Account, prices: Map<StockSymbol, Decimal>): Decimal {
    return account.stocks
      .map((e) => AssetService.evaluateStock(e, prices))
      .reduce((acc, v) => acc.plus(v), new Decimal(0))
      .plus(account.cash);
  },
};
