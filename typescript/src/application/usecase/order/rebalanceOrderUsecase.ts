import { AccountRepository } from "../../repository/accountRepository";
import { PortfolioRepository } from "../../repository/portfolioRepository";
import { UserId } from "../../../domain/userId";

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
  ) {}

  async run(input: RebalanceOrderUsecaseInput): Promise<void> {
    const account = await this.accountRepository.find(input.userId);
    if (account === undefined) {
      throw new RebalanceUserNotFoundException();
    }
    const portfolio = await this.portfolioRepository.get();
    const updated = account.rebalance(portfolio);
    await this.accountRepository.upsert(input.userId, updated);
  }
}
