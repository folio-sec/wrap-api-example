import Decimal from "decimal.js";
import { StockSymbol } from "./stockSymbol";

// Stock は保有銘柄（銘柄と保有額）を表す。
export interface Stock {
  symbol: StockSymbol;
  amountJpy: Decimal;
}

export interface PortfolioItem {
  symbol: StockSymbol;
  rate: Decimal;
}

// Portfolio は最適ポートフォリオ（銘柄ごとの構成比率）を表す。
export class Portfolio {
  readonly items: ReadonlyArray<PortfolioItem>;

  constructor(items: ReadonlyArray<PortfolioItem>) {
    if (items.length === 0) {
      throw new Error("portfolio must have at least one item");
    }
    const sum = items.reduce((acc, i) => acc.plus(i.rate), new Decimal(0));
    if (!sum.equals(1)) {
      throw new Error(`portfolio rates must sum to 1, got ${sum.toString()}`);
    }
    const symbols = new Set(items.map((i) => i.symbol));
    if (symbols.size !== items.length) {
      throw new Error("portfolio must not have duplicate symbols");
    }
    this.items = items;
  }
}
