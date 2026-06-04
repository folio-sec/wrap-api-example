import Decimal from "decimal.js";
import { MarketPriceRepository } from "../../application/repository/marketPriceRepository.js";
import { AppConstants } from "../../domain/appConstants.js";
import { StockSymbol } from "../../domain/stockSymbol.js";

export class MarketPriceRepositoryImpl implements MarketPriceRepository {
  private prices: Map<StockSymbol, Decimal> = new Map(AppConstants.initialPrices);

  async all(): Promise<Map<StockSymbol, Decimal>> {
    return new Map(this.prices);
  }

  async update(prices: Map<StockSymbol, Decimal>): Promise<void> {
    this.prices = new Map(prices);
  }
}
