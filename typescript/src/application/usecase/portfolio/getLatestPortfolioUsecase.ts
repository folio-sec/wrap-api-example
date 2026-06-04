import Decimal from "decimal.js";
import { PortfolioRepository } from "../../repository/portfolioRepository.js";
import { StockSymbol } from "../../../domain/stockSymbol.js";

export interface GetLatestPortfolioItemOutput {
  symbol: StockSymbol;
  rate: Decimal;
}

export interface GetLatestPortfolioUsecaseOutput {
  items: GetLatestPortfolioItemOutput[];
}

export class GetLatestPortfolioUsecase {
  constructor(private readonly portfolioRepository: PortfolioRepository) {}

  async run(): Promise<GetLatestPortfolioUsecaseOutput> {
    const p = await this.portfolioRepository.get();
    return {
      items: p.items.map((i) => ({ symbol: i.symbol, rate: i.rate })),
    };
  }
}
