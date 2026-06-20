import Decimal from "decimal.js";
import { PortfolioRepository } from "../../repository/portfolioRepository";
import { StockSymbol } from "../../../domain/stockSymbol";

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
