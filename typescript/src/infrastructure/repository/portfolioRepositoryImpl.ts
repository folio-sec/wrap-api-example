import { PortfolioRepository } from "../../application/repository/portfolioRepository";
import { AppConstants } from "../../domain/appConstants";
import { Portfolio } from "../../domain/stock";

export class PortfolioRepositoryImpl implements PortfolioRepository {
  private portfolio: Portfolio = AppConstants.initialPortfolio;

  async get(): Promise<Portfolio> {
    return this.portfolio;
  }

  async update(portfolio: Portfolio): Promise<void> {
    this.portfolio = portfolio;
  }
}
