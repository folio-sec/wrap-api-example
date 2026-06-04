import Decimal from "decimal.js";
import { StockSymbol } from "./stockSymbol.js";
import { Portfolio } from "./stock.js";

export const AppConstants = {
  cashRate: new Decimal("0.05"),
  minOperationAmount: new Decimal(10000),
  supportedSymbols: [StockSymbol.Toyopa, StockSymbol.Somy] as ReadonlyArray<StockSymbol>,
  initialPrices: new Map<StockSymbol, Decimal>([
    [StockSymbol.Toyopa, new Decimal("4.2135")],
    [StockSymbol.Somy, new Decimal("1.2345")],
  ]),
  initialPortfolio: new Portfolio([
    { symbol: StockSymbol.Toyopa, rate: new Decimal("0.40") },
    { symbol: StockSymbol.Somy, rate: new Decimal("0.60") },
  ]),
};
