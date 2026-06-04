import Decimal from "decimal.js";
import { AccountRepository } from "../../repository/accountRepository.js";
import { MarketPriceRepository } from "../../repository/marketPriceRepository.js";
import { PortfolioRepository } from "../../repository/portfolioRepository.js";
import { PortfolioService } from "../../service/portfolioService.js";
import { AppConstants } from "../../../domain/appConstants.js";
import { UserId } from "../../../domain/userId.js";

export interface NewContributionOrderUsecaseInput {
  userId: UserId;
  amount: Decimal;
}

export class NewContributionOrderUsecaseException extends Error {}
export class NewContributionUserAlreadyExistsException extends NewContributionOrderUsecaseException {
  constructor() {
    super("user already exists");
  }
}
export class NewContributionAmountTooSmallException extends NewContributionOrderUsecaseException {
  constructor() {
    super("amount too small");
  }
}

export class NewContributionOrderUsecase {
  constructor(
    private readonly accountRepository: AccountRepository,
    private readonly portfolioRepository: PortfolioRepository,
    private readonly marketPriceRepository: MarketPriceRepository,
  ) {}

  async run(input: NewContributionOrderUsecaseInput): Promise<void> {
    if (input.amount.lessThan(AppConstants.minOperationAmount)) {
      throw new NewContributionAmountTooSmallException();
    }
    const exists = await this.accountRepository.exists(input.userId);
    if (exists) {
      throw new NewContributionUserAlreadyExistsException();
    }
    const portfolio = await this.portfolioRepository.get();
    const prices = await this.marketPriceRepository.all();
    const account = PortfolioService.allocateNew(input.amount, portfolio, prices);
    await this.accountRepository.upsert(input.userId, account);
  }
}
