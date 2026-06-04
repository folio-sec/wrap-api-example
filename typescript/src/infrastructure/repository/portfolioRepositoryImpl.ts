import { PortfolioRepository } from "../../application/repository/portfolioRepository.js";
import { AppConstants } from "../../domain/appConstants.js";
import { Portfolio } from "../../domain/stock.js";

export class PortfolioRepositoryImpl implements PortfolioRepository {
  private portfolio: Portfolio = AppConstants.initialPortfolio;

  async get(): Promise<Portfolio> {
    return this.portfolio;
  }

  async update(portfolio: Portfolio): Promise<void> {
    this.portfolio = portfolio;
  }
}
