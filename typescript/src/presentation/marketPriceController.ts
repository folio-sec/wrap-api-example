import Decimal from "decimal.js";
import {
  UpdateMarketPriceItemInput,
  UpdateMarketPriceUsecase,
} from "../application/usecase/market_price/updateMarketPriceUsecase.js";
import { StockSymbol } from "../domain/stockSymbol.js";
import { BadRequestException } from "./presentationException.js";

export interface MarketPriceItemDto {
  symbol: string;
  market_price: string;
}

export interface UpdateMarketPriceRequest {
  market_prices: MarketPriceItemDto[];
}

export class MarketPriceController {
  constructor(private readonly updateMarketPriceUsecase: UpdateMarketPriceUsecase) {}

  async updateMarketPrice(req: UpdateMarketPriceRequest): Promise<void> {
    const items: UpdateMarketPriceItemInput[] = [];
    for (const dto of req.market_prices) {
      const sym = StockSymbol.fromString(dto.symbol);
      if (sym === undefined) {
        throw new BadRequestException(`unknown symbol: ${dto.symbol}`);
      }
      let price: Decimal;
      try {
        price = new Decimal(dto.market_price);
      } catch {
        throw new BadRequestException(`invalid market_price: ${dto.market_price}`);
      }
      items.push({ symbol: sym, marketPrice: price });
    }
    await this.updateMarketPriceUsecase.run({ items });
  }
}
