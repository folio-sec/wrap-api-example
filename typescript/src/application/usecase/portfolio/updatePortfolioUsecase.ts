import Decimal from "decimal.js";
import { PortfolioRepository } from "../../repository/portfolioRepository.js";
import { Portfolio } from "../../../domain/stock.js";
import { StockSymbol } from "../../../domain/stockSymbol.js";

export interface UpdatePortfolioItemInput {
  symbol: StockSymbol;
  rate: Decimal;
}

export interface UpdatePortfolioUsecaseInput {
  items: UpdatePortfolioItemInput[];
}

export class UpdatePortfolioUsecaseException extends Error {}
export class InvalidPortfolioException extends UpdatePortfolioUsecaseException {
  constructor(reason: string) {
    super(reason);
  }
}

export class UpdatePortfolioUsecase {
  constructor(private readonly portfolioRepository: PortfolioRepository) {}

  async run(input: UpdatePortfolioUsecaseInput): Promise<void> {
    let portfolio: Portfolio;
    try {
      portfolio = new Portfolio(input.items.map((i) => ({ symbol: i.symbol, rate: i.rate })));
    } catch (e) {
      const msg = e instanceof Error ? e.message : String(e);
      throw new InvalidPortfolioException(msg);
    }
    await this.portfolioRepository.update(portfolio);
  }
}
