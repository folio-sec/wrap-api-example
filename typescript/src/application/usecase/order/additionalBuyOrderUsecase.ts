import Decimal from "decimal.js";
import { AccountRepository } from "../../repository/accountRepository.js";
import { PortfolioRepository } from "../../repository/portfolioRepository.js";
import { AppConstants } from "../../../domain/appConstants.js";
import { UserId } from "../../../domain/userId.js";

export interface AdditionalBuyOrderUsecaseInput {
  userId: UserId;
  amount: Decimal;
}

export class AdditionalBuyOrderUsecaseException extends Error {}
export class AdditionalBuyUserNotFoundException extends AdditionalBuyOrderUsecaseException {
  constructor() {
    super("user not found");
  }
}
export class AdditionalBuyAmountTooSmallException extends AdditionalBuyOrderUsecaseException {
  constructor() {
    super("amount too small");
  }
}

export class AdditionalBuyOrderUsecase {
  constructor(
    private readonly accountRepository: AccountRepository,
    private readonly portfolioRepository: PortfolioRepository,
  ) {}

  async run(input: AdditionalBuyOrderUsecaseInput): Promise<void> {
    if (input.amount.lessThan(AppConstants.minOperationAmount)) {
      throw new AdditionalBuyAmountTooSmallException();
    }
    const account = await this.accountRepository.find(input.userId);
    if (account === undefined) {
      throw new AdditionalBuyUserNotFoundException();
    }
    const portfolio = await this.portfolioRepository.get();
    const updated = account.addFunds(input.amount, portfolio);
    await this.accountRepository.upsert(input.userId, updated);
  }
}
