import Decimal from "decimal.js";
import { MarketPriceRepository } from "../../repository/marketPriceRepository.js";
import { StockSymbol } from "../../../domain/stockSymbol.js";

export interface UpdateMarketPriceItemInput {
  symbol: StockSymbol;
  marketPrice: Decimal;
}

export interface UpdateMarketPriceUsecaseInput {
  items: UpdateMarketPriceItemInput[];
}

export class UpdateMarketPriceUsecase {
  constructor(private readonly marketPriceRepository: MarketPriceRepository) {}

  async run(input: UpdateMarketPriceUsecaseInput): Promise<void> {
    const prices = new Map<StockSymbol, Decimal>(
      input.items.map((i) => [i.symbol, i.marketPrice]),
    );
    await this.marketPriceRepository.update(prices);
  }
}
