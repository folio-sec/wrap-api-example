import Decimal from "decimal.js";
import { AppConstants } from "../../domain/appConstants.js";
import { Account, Stock, Portfolio } from "../../domain/stock.js";
import { StockSymbol } from "../../domain/stockSymbol.js";
import { AssetService } from "./assetService.js";

const floor2 = (x: Decimal): Decimal => x.toDecimalPlaces(2, Decimal.ROUND_DOWN);
const floor0 = (x: Decimal): Decimal => x.toDecimalPlaces(0, Decimal.ROUND_DOWN);
const priceOf = (prices: Map<StockSymbol, Decimal>, symbol: StockSymbol): Decimal => {
  const p = prices.get(symbol);
  if (p === undefined) throw new Error(`missing price for ${symbol}`);
  return p;
};

export const PortfolioService = {
  /** Allocate a brand-new account given a contribution amount. */
  allocateNew(
    amount: Decimal,
    portfolio: Portfolio,
    prices: Map<StockSymbol, Decimal>,
  ): Account {
    const cashFromRate = floor0(amount.times(AppConstants.cashRate));
    const investable = amount.minus(cashFromRate);
    const stocks: Stock[] = portfolio.items.map((item) => {
      const price = priceOf(prices, item.symbol);
      const qty = floor2(investable.times(item.rate).div(price));
      return { symbol: item.symbol, qty };
    });
    const usedForStocks = stocks
      .map((e) => e.qty.times(priceOf(prices, e.symbol)))
      .reduce((acc, v) => acc.plus(v), new Decimal(0));
    const residual = investable.minus(usedForStocks);
    return { cash: cashFromRate.plus(residual), stocks };
  },

  /** Additional contribution: only buy (no sell). Residual is kept in cash. */
  allocateAdditional(
    account: Account,
    amount: Decimal,
    portfolio: Portfolio,
    prices: Map<StockSymbol, Decimal>,
  ): Account {
    const totalAfter = AssetService.totalValuation(account, prices).plus(amount);
    const targetCash = floor0(totalAfter.times(AppConstants.cashRate));
    const investable = totalAfter.minus(targetCash);
    const currentQty = new Map<StockSymbol, Decimal>(
      account.stocks.map((e) => [e.symbol, e.qty]),
    );

    const portfolioSymbols = new Set(portfolio.items.map((i) => i.symbol));
    const newPortfolioStocks: Stock[] = portfolio.items.map((item) => {
      const price = priceOf(prices, item.symbol);
      const targetQty = floor2(investable.times(item.rate).div(price));
      const current = currentQty.get(item.symbol) ?? new Decimal(0);
      const finalQty = targetQty.greaterThan(current) ? targetQty : current;
      return { symbol: item.symbol, qty: finalQty };
    });
    const preservedStocks = account.stocks.filter((e) => !portfolioSymbols.has(e.symbol));
    const allStocks = [...newPortfolioStocks, ...preservedStocks];

    const finalValuation = allStocks
      .map((e) => e.qty.times(priceOf(prices, e.symbol)))
      .reduce((acc, v) => acc.plus(v), new Decimal(0));
    const finalCash = totalAfter.minus(finalValuation);
    return { cash: finalCash, stocks: allStocks };
  },

  /** Rebalance: re-allocate qty per portfolio target (buy and sell). */
  rebalance(
    account: Account,
    portfolio: Portfolio,
    prices: Map<StockSymbol, Decimal>,
  ): Account {
    // XXX this implementation might not be correct
    const investable = AssetService.totalValuation(account, prices);
    const newStocks: Stock[] = portfolio.items.map((item) => {
      const price = priceOf(prices, item.symbol);
      const qty = floor2(investable.times(item.rate).div(price));
      return { symbol: item.symbol, qty };
    });
    const finalValuation = newStocks
      .map((e) => e.qty.times(priceOf(prices, e.symbol)))
      .reduce((acc, v) => acc.plus(v), new Decimal(0));
    const finalCash = investable.minus(finalValuation);
    return { cash: finalCash, stocks: newStocks };
  },
};
