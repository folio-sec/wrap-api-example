import Decimal from "decimal.js";
import { Portfolio, Stock } from "./stock";
import { StockSymbol } from "./stockSymbol";
import { AppConstants } from "./appConstants";

// floor0 は円未満を切り捨てる（資産配分はすべて円単位で行う）。
const floor0 = (x: Decimal): Decimal => x.toDecimalPlaces(0, Decimal.ROUND_DOWN);

// Account は口座を表す。
export class Account {
  constructor(
    readonly cash: Decimal,
    readonly stocks: ReadonlyArray<Stock>,
  ) {}

  // total は口座の総資産（現金 + 各銘柄の保有額）を返す。
  total(): Decimal {
    return this.stocks.reduce((acc, s) => acc.plus(s.amountJpy), this.cash);
  }

  // openAccount は新規注文額を、最適ポートフォリオに沿って配分した口座を生成する。
  static openAccount(amount: Decimal, portfolio: Portfolio): Account {
    const cashFromRate = floor0(amount.times(AppConstants.cashRate));
    const investable = amount.minus(cashFromRate);
    const stocks: Stock[] = [];
    let usedForStocks = new Decimal(0);
    for (const item of portfolio.items) {
      const amt = floor0(investable.times(item.rate));
      stocks.push({ symbol: item.symbol, amountJpy: amt });
      usedForStocks = usedForStocks.plus(amt);
    }
    const residual = investable.minus(usedForStocks);
    return new Account(cashFromRate.plus(residual), stocks);
  }

  // addFunds は追加注文額を口座へ反映する。最適ポートフォリオの目標額を下回らない範囲で
  // 既存の保有額を維持し、ポートフォリオ外の銘柄はそのまま保持する。
  addFunds(amount: Decimal, portfolio: Portfolio): Account {
    const totalAfter = this.total().plus(amount);
    const targetCash = floor0(totalAfter.times(AppConstants.cashRate));
    const investable = totalAfter.minus(targetCash);

    const currentAmount = new Map<StockSymbol, Decimal>(
      this.stocks.map((s) => [s.symbol, s.amountJpy]),
    );
    const portfolioSymbols = new Set(portfolio.items.map((i) => i.symbol));

    const newPortfolioStocks: Stock[] = portfolio.items.map((item) => {
      const target = floor0(investable.times(item.rate));
      const current = currentAmount.get(item.symbol) ?? new Decimal(0);
      const final = current.greaterThan(target) ? current : target;
      return { symbol: item.symbol, amountJpy: final };
    });

    const preservedStocks = this.stocks.filter((s) => !portfolioSymbols.has(s.symbol));
    const allStocks = [...newPortfolioStocks, ...preservedStocks];
    const finalAmount = allStocks.reduce((acc, s) => acc.plus(s.amountJpy), new Decimal(0));
    const finalCash = totalAfter.minus(finalAmount);
    return new Account(finalCash, allStocks);
  }

  // rebalance は保有資産を最適ポートフォリオの比率に近づける。
  rebalance(portfolio: Portfolio): Account {
    // XXX this implementation might not be correct
    const investable = this.total();
    const newStocks: Stock[] = [];
    let usedForStocks = new Decimal(0);
    for (const item of portfolio.items) {
      const amt = floor0(investable.times(item.rate));
      newStocks.push({ symbol: item.symbol, amountJpy: amt });
      usedForStocks = usedForStocks.plus(amt);
    }
    const finalCash = investable.minus(usedForStocks);
    return new Account(finalCash, newStocks);
  }
}
