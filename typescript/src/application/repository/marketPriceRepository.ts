import Decimal from "decimal.js";
import { StockSymbol } from "../../domain/stockSymbol.js";

/** 市場価格リポジトリ。 */
export interface MarketPriceRepository {
  all(): Promise<Map<StockSymbol, Decimal>>;
  update(prices: Map<StockSymbol, Decimal>): Promise<void>;
}
