import { AccountRepository } from "../../repository/accountRepository.js";
import { MarketPriceRepository } from "../../repository/marketPriceRepository.js";
import { PortfolioRepository } from "../../repository/portfolioRepository.js";
import { PortfolioService } from "../../service/portfolioService.js";
import { UserId } from "../../../domain/userId.js";

export interface RebalanceOrderUsecaseInput {
  userId: UserId;
}

export class RebalanceOrderUsecaseException extends Error {}
export class RebalanceUserNotFoundException extends RebalanceOrderUsecaseException {
  constructor() {
    super("user not found");
  }
}

export class RebalanceOrderUsecase {
  constructor(
    private readonly accountRepository: AccountRepository,
    private readonly portfolioRepository: PortfolioRepository,
    private readonly marketPriceRepository: MarketPriceRepository,
  ) {}

  async run(input: RebalanceOrderUsecaseInput): Promise<void> {
    const account = await this.accountRepository.find(input.userId);
    if (account === undefined) {
      throw new RebalanceUserNotFoundException();
    }
    const portfolio = await this.portfolioRepository.get();
    const prices = await this.marketPriceRepository.all();
    const updated = PortfolioService.rebalance(account, portfolio, prices);
    await this.accountRepository.upsert(input.userId, updated);
  }
}
