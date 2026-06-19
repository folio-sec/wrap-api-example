import Decimal from "decimal.js";
import { AccountRepository } from "../../repository/accountRepository.js";
import { PortfolioRepository } from "../../repository/portfolioRepository.js";
import { Account } from "../../../domain/account.js";
import { AppConstants } from "../../../domain/appConstants.js";
import { UserId } from "../../../domain/userId.js";

export interface NewOrderUsecaseInput {
  userId: UserId;
  amount: Decimal;
}

export class NewOrderUsecaseException extends Error {}
export class NewOrderUserAlreadyExistsException extends NewOrderUsecaseException {
  constructor() {
    super("user already exists");
  }
}
export class NewOrderAmountTooSmallException extends NewOrderUsecaseException {
  constructor() {
    super("amount too small");
  }
}

export class NewOrderUsecase {
  constructor(
    private readonly accountRepository: AccountRepository,
    private readonly portfolioRepository: PortfolioRepository,
  ) {}

  async run(input: NewOrderUsecaseInput): Promise<void> {
    if (input.amount.lessThan(AppConstants.minOperationAmount)) {
      throw new NewOrderAmountTooSmallException();
    }
    const exists = await this.accountRepository.exists(input.userId);
    if (exists) {
      throw new NewOrderUserAlreadyExistsException();
    }
    const portfolio = await this.portfolioRepository.get();
    const account = Account.openAccount(input.amount, portfolio);
    await this.accountRepository.upsert(input.userId, account);
  }
}
