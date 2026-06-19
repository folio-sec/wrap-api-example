import Decimal from "decimal.js";
import { StockSymbol } from "./stockSymbol.js";
import { Portfolio } from "./stock.js";

/** アプリケーション定数。 */
export const AppConstants = {
  cashRate: new Decimal("0.05"),
  minOperationAmount: new Decimal(10000),
  supportedSymbols: [StockSymbol.Toyopa, StockSymbol.Somy] as ReadonlyArray<StockSymbol>,
  initialPortfolio: new Portfolio([
    { symbol: StockSymbol.Toyopa, rate: new Decimal("0.40") },
    { symbol: StockSymbol.Somy, rate: new Decimal("0.60") },
  ]),
};
